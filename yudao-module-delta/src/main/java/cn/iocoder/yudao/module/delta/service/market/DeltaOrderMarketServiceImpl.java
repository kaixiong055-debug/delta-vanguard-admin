package cn.iocoder.yudao.module.delta.service.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaOrderMarketLockRedisDAO;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_EXPIRED;
import static cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.*;

/**
 * 平台订单市场 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaOrderMarketServiceImpl implements DeltaOrderMarketService {

    @Resource
    private DeltaOrderMarketListingMapper deltaOrderMarketListingMapper;
    @Resource
    private DeltaOrderMarketLogMapper deltaOrderMarketLogMapper;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaClubProfileMapper deltaClubProfileMapper;
    @Resource
    private DeltaClubServiceScopeMapper deltaClubServiceScopeMapper;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;
    @Resource
    private DeltaEventPublisher deltaEventPublisher;
    @Resource
    private DeltaOrderMarketLockRedisDAO deltaOrderMarketLockRedisDAO;
    @Resource
    private DeltaOrderMarketEligibilityService eligibilityService;

    /** 可发布到市场的服务订单状态: 待派单(10) */
    private static final int PUBLISHABLE_ORDER_STATUS = 10;

    /** 市场事件聚合类型 */
    private static final String MARKET_AGGREGATE_TYPE = "ORDER_MARKET_LISTING";

    // ========== 平台挂牌操作 ==========

    @Override
    public PageResult<DeltaOrderMarketListingDO> getListingPage(DeltaOrderMarketListingPageReqVO reqVO) {
        return TenantUtils.executeIgnore(() ->
                deltaOrderMarketListingMapper.selectPage(
                        reqVO.getPageNo(), reqVO.getPageSize(),
                        reqVO.getListingNo(), reqVO.getServiceOrderNo(),
                        reqVO.getServiceType(), reqVO.getListingStatus(),
                        reqVO.getClaimedClubId(), reqVO.getClaimedClubTenantId(),
                        reqVO.getPublishTime(), reqVO.getCreateTime()));
    }

    @Override
    public DeltaOrderMarketListingDO getListing(Long id) {
        return TenantUtils.executeIgnore(() ->
                deltaOrderMarketListingMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderMarketListingDO publish(DeltaOrderMarketPublishReqVO reqVO, Long publisherId) {
        // 0. 获取 distributed lock on serviceOrderId to prevent concurrent publish
        return deltaOrderMarketLockRedisDAO.lockAndRunOnPublish(reqVO.getServiceOrderId(), () -> {
            // 1. 查询服务订单（锁内重新读取）
            DeltaServiceOrderDO order = TenantUtils.executeIgnore(() ->
                    deltaServiceOrderMapper.selectById(reqVO.getServiceOrderId()));
            if (order == null) {
                throw exception(ORDER_MARKET_ORDER_NOT_EXISTS);
            }
            // 2. 状态校验：只有待派单(10)可以发布
            if (order.getStatus() == null || order.getStatus() != PUBLISHABLE_ORDER_STATUS) {
                throw exception(ORDER_MARKET_ORDER_STATUS_NOT_ALLOWED);
            }
            // 3. 检查是否已有 AVAILABLE 挂牌（锁内防重）
            DeltaOrderMarketListingDO existing = TenantUtils.executeIgnore(() ->
                    deltaOrderMarketListingMapper.selectByServiceOrderId(reqVO.getServiceOrderId()));
            if (existing != null && DeltaOrderMarketStatusEnum.AVAILABLE.getStatus().equals(existing.getListingStatus())) {
                throw exception(ORDER_MARKET_ALREADY_PUBLISHED);
            }
            // 检查是否已有 CLAIMED 挂牌
            if (existing != null && DeltaOrderMarketStatusEnum.CLAIMED.getStatus().equals(existing.getListingStatus())) {
                throw exception(ORDER_MARKET_ORDER_ALREADY_ASSIGNED);
            }

            // 4. 生成挂牌编号
            String listingNo = deltaNoRedisDAO.generateMarketListingNo();
            LocalDateTime now = LocalDateTime.now();

            // 5. 创建挂牌
            DeltaOrderMarketListingDO listing = DeltaOrderMarketListingDO.builder()
                    .listingNo(listingNo)
                    .serviceOrderId(order.getId())
                    .serviceOrderNo(order.getServiceOrderNo())
                    .sourceTenantId(order.getTenantId())
                    .serviceType(order.getServiceType())
                    .serviceAmount(order.getServiceAmount())
                    .requirementSummary(reqVO.getRequirementSummary())
                    .listingStatus(DeltaOrderMarketStatusEnum.AVAILABLE.getStatus())
                    .publishTime(now)
                    .expireTime(reqVO.getExpireTime())
                    .publisherId(publisherId)
                    .remark(reqVO.getRemark())
                    .activeFlag(1)
                    .version(0)
                    .build();
            deltaOrderMarketListingMapper.insert(listing);

            // 6. 写操作日志
            writeLog(listing, DeltaOrderMarketOperationTypeEnum.PUBLISH.getType(), "PLATFORM",
                    publisherId, null, null, null, null, true, null, "挂牌发布");

            // 7. 发布 Outbox 事件
            publishEvent(ORDER_MARKET_PUBLISHED, listing, null, null);

            log.info("挂牌发布成功 listingNo={}, serviceOrderId={}, publisherId={}",
                    listingNo, order.getId(), publisherId);
            return listing;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(DeltaOrderMarketWithdrawReqVO reqVO, Long operatorId) {
        // 1. 查询挂牌
        DeltaOrderMarketListingDO listing = deltaOrderMarketListingMapper.selectById(reqVO.getId());
        if (listing == null) {
            throw exception(ORDER_MARKET_LISTING_NOT_EXISTS);
        }
        // 2. 仅 AVAILABLE 可撤回
        if (!DeltaOrderMarketStatusEnum.AVAILABLE.getStatus().equals(listing.getListingStatus())) {
            throw exception(ORDER_MARKET_STATUS_NOT_ALLOWED);
        }
        // 3. CAS 更新
        int rows = deltaOrderMarketListingMapper.updateStatusCas(
                listing.getId(), listing.getVersion(),
                DeltaOrderMarketStatusEnum.AVAILABLE.getStatus(),
                DeltaOrderMarketStatusEnum.WITHDRAWN.getStatus());
        if (rows == 0) {
            throw exception(ORDER_MARKET_CAS_FAILED);
        }
        // 更新 listing 的状态
        listing.setListingStatus(DeltaOrderMarketStatusEnum.WITHDRAWN.getStatus());
        listing.setWithdrawReason(reqVO.getReason());

        // 4. 写日志
        writeLog(listing, DeltaOrderMarketOperationTypeEnum.WITHDRAW.getType(), "PLATFORM",
                operatorId, null, null,
                DeltaOrderMarketStatusEnum.AVAILABLE.getStatus(),
                DeltaOrderMarketStatusEnum.WITHDRAWN.getStatus(),
                true, null, reqVO.getReason());

        // 5. 发布 Outbox
        publishEvent(ORDER_MARKET_WITHDRAWN, listing, null, null);

        log.info("挂牌撤回成功 listingNo={}, operatorId={}", listing.getListingNo(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(DeltaOrderMarketAssignReqVO reqVO, Long operatorId) {
        // 1. 查询挂牌
        DeltaOrderMarketListingDO listing = deltaOrderMarketListingMapper.selectById(reqVO.getId());
        if (listing == null) {
            throw exception(ORDER_MARKET_LISTING_NOT_EXISTS);
        }

        // 2. 查询俱乐部获取 tenantId（跨租户）
        DeltaClubProfileDO assignClub = TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectById(reqVO.getClubId()));
        if (assignClub == null) {
            throw exception(ORDER_MARKET_CLUB_NOT_EXISTS);
        }

        // 3. 资格校验（复用统一校验：包括服务订单状态再次确认）
        eligibilityService.checkEligibility(listing, reqVO.getClubId(), assignClub.getTenantId());

        // 4. 分布式锁内 CAS 更新（与 claim 共用同一把锁）
        deltaOrderMarketLockRedisDAO.lockAndRun(listing.getId(), () -> {
            // 锁内重新查询
            DeltaOrderMarketListingDO current = deltaOrderMarketListingMapper.selectById(listing.getId());
            if (current == null || !DeltaOrderMarketStatusEnum.AVAILABLE.getStatus().equals(current.getListingStatus())) {
                throw exception(ORDER_MARKET_CAS_FAILED);
            }
            // 锁内再次校验服务订单状态（防止订单在锁外被变更）
            eligibilityService.recheckOrderStatus(current.getServiceOrderId());

            LocalDateTime now = LocalDateTime.now();
            int rows = deltaOrderMarketListingMapper.updateClaimCas(
                    current.getId(), current.getVersion(),
                    reqVO.getClubId(), assignClub.getTenantId(), now);
            if (rows == 0) {
                throw exception(ORDER_MARKET_CAS_FAILED);
            }
            current.setListingStatus(DeltaOrderMarketStatusEnum.CLAIMED.getStatus());
            current.setClaimedClubId(reqVO.getClubId());
            current.setClaimedClubTenantId(assignClub.getTenantId());
            current.setClaimTime(now);

            // 写日志
            writeLog(current, DeltaOrderMarketOperationTypeEnum.ASSIGN.getType(), "PLATFORM",
                    operatorId, reqVO.getClubId(), assignClub.getTenantId(),
                    DeltaOrderMarketStatusEnum.AVAILABLE.getStatus(),
                    DeltaOrderMarketStatusEnum.CLAIMED.getStatus(),
                    true, null, reqVO.getRemark());

            // 发布 Outbox
            publishEvent(ORDER_MARKET_ASSIGNED, current, reqVO.getClubId(), assignClub.getTenantId());

            return null;
        });

        log.info("平台指定俱乐部成功 listingNo={}, clubId={}, operatorId={}",
                listing.getListingNo(), reqVO.getClubId(), operatorId);
    }

    // ========== 俱乐部操作 ==========

    @Override
    public PageResult<DeltaOrderMarketListingDO> getAvailablePage(Integer pageNo, Integer pageSize) {
        DeltaClubProfileDO club = eligibilityService.getAndValidateCurrentClub();
        return getAvailablePageForClub(club, pageNo, pageSize);
    }

    @Override
    public PageResult<DeltaOrderMarketListingDO> getAvailablePageForMember(
            Long memberUserId, Integer pageNo, Integer pageSize) {
        DeltaClubProfileDO club =
                eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        return getAvailablePageForClub(club, pageNo, pageSize);
    }

    private PageResult<DeltaOrderMarketListingDO> getAvailablePageForClub(
            DeltaClubProfileDO club, Integer pageNo, Integer pageSize) {
        List<DeltaClubServiceScopeDO> scopes = TenantUtils.executeIgnore(() ->
                deltaClubServiceScopeMapper.selectListByClubProfileId(club.getId()));
        Set<Integer> enabledServiceTypes = scopes.stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled())
                .map(DeltaClubServiceScopeDO::getServiceType)
                .collect(Collectors.toSet());

        if (enabledServiceTypes.isEmpty()) {
            return PageResult.empty();
        }

        // 在数据库层面按服务类型过滤可用挂牌（避免先全表查询再Java过滤）
        LocalDateTime now = LocalDateTime.now();
        return deltaOrderMarketListingMapper.selectAvailablePageByServiceTypes(
                pageNo, pageSize, now, enabledServiceTypes);
    }

    @Override
    public DeltaOrderMarketListingDO getAvailable(Long id) {
        DeltaClubProfileDO club = eligibilityService.getAndValidateCurrentClub();
        return getAvailableForClub(club, id);
    }

    @Override
    public DeltaOrderMarketListingDO getAvailableForMember(Long memberUserId, Long listingId) {
        DeltaClubProfileDO club =
                eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        return getAvailableForClub(club, listingId);
    }

    private DeltaOrderMarketListingDO getAvailableForClub(DeltaClubProfileDO club, Long listingId) {
        DeltaOrderMarketListingDO listing = deltaOrderMarketListingMapper.selectById(listingId);
        if (listing == null) {
            throw exception(ORDER_MARKET_LISTING_NOT_EXISTS);
        }
        eligibilityService.checkEligibility(listing, club.getId(), club.getTenantId());
        return listing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long listingId) {
        DeltaClubProfileDO club = eligibilityService.getAndValidateCurrentClub();
        doClaim(listingId, club);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimForMember(Long memberUserId, Long listingId) {
        DeltaClubProfileDO club =
                eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        doClaim(listingId, club);
    }

    private void doClaim(Long listingId, DeltaClubProfileDO club) {
        DeltaOrderMarketListingDO listing = deltaOrderMarketListingMapper.selectById(listingId);
        if (listing == null) {
            throw exception(ORDER_MARKET_LISTING_NOT_EXISTS);
        }
        eligibilityService.checkEligibility(listing, club.getId(), club.getTenantId());
        deltaOrderMarketLockRedisDAO.lockAndRun(listingId, () -> {
            // 锁内重新查询
            DeltaOrderMarketListingDO current = deltaOrderMarketListingMapper.selectById(listingId);
            if (current == null || !DeltaOrderMarketStatusEnum.AVAILABLE.getStatus().equals(current.getListingStatus())) {
                writeClaimFailedLog(current != null ? current : listing, club, "挂牌状态已变化");
                throw exception(ORDER_MARKET_CAS_FAILED);
            }
            // 再次校验过期
            if (current.getExpireTime() != null && current.getExpireTime().isBefore(LocalDateTime.now())) {
                writeClaimFailedLog(current, club, "挂牌已过期");
                throw exception(ORDER_MARKET_EXPIRED);
            }
            // 锁内再次校验服务订单状态（防止订单在锁外被变更/取消/派单）
            eligibilityService.recheckOrderStatus(current.getServiceOrderId());
            // 锁内再次校验服务范围和容量（确保不会race condition）
            eligibilityService.recheckClubCapacity(club.getId(), club.getTenantId(), club.getMaxConcurrentOrders());

            // CAS 更新
            LocalDateTime now = LocalDateTime.now();
            int rows = deltaOrderMarketListingMapper.updateClaimCas(
                    current.getId(), current.getVersion(),
                    club.getId(), club.getTenantId(), now);
            if (rows == 0) {
                writeClaimFailedLog(current, club, "CAS更新失败");
                throw exception(ORDER_MARKET_CAS_FAILED);
            }

            current.setListingStatus(DeltaOrderMarketStatusEnum.CLAIMED.getStatus());
            current.setClaimedClubId(club.getId());
            current.setClaimedClubTenantId(club.getTenantId());
            current.setClaimTime(now);

            // 写日志
            writeLog(current, DeltaOrderMarketOperationTypeEnum.CLAIM.getType(), "CLUB",
                    null, club.getId(), club.getTenantId(),
                    DeltaOrderMarketStatusEnum.AVAILABLE.getStatus(),
                    DeltaOrderMarketStatusEnum.CLAIMED.getStatus(),
                    true, null, "俱乐部抢单");

            // 发布 Outbox 事件
            publishEvent(ORDER_MARKET_CLAIMED, current, club.getId(), club.getTenantId());

            log.info("俱乐部抢单成功 listingNo={}, clubId={}, clubTenantId={}",
                    current.getListingNo(), club.getId(), club.getTenantId());
            return null;
        });
    }

    @Override
    public PageResult<DeltaOrderMarketListingDO> getMyClaimedPage(Integer pageNo, Integer pageSize) {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        return deltaOrderMarketListingMapper.selectClaimedPage(pageNo, pageSize, tenantId);
    }

    @Override
    public PageResult<DeltaOrderMarketListingDO> getMyClaimedPageForMember(
            Long memberUserId, Integer pageNo, Integer pageSize) {
        DeltaClubProfileDO club =
                eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        return deltaOrderMarketListingMapper.selectClaimedPage(
                pageNo, pageSize, club.getTenantId());
    }

    // ========== 日志 ==========

    @Override
    public List<DeltaOrderMarketLogDO> getListingLogs(Long listingId) {
        return deltaOrderMarketLogMapper.selectByListingId(listingId);
    }

    // ========== 私有方法 ==========

    private void writeLog(DeltaOrderMarketListingDO listing, String operationType, String operatorType,
                          Long operatorId, Long clubId, Long clubTenantId,
                          Integer beforeStatus, Integer afterStatus,
                          boolean success, String failureReason, String remark) {
        DeltaOrderMarketLogDO logDO = DeltaOrderMarketLogDO.builder()
                .listingId(listing.getId())
                .serviceOrderId(listing.getServiceOrderId())
                .operationType(operationType)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .clubId(clubId)
                .clubTenantId(clubTenantId)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .success(success ? 1 : 0)
                .failureReason(failureReason)
                .remark(remark)
                .build();
        deltaOrderMarketLogMapper.insert(logDO);
    }

    private void writeClaimFailedLog(DeltaOrderMarketListingDO listing, DeltaClubProfileDO club, String reason) {
        writeLog(listing, DeltaOrderMarketOperationTypeEnum.CLAIM_FAILED.getType(), "CLUB",
                null, club.getId(), club.getTenantId(),
                listing.getListingStatus(), listing.getListingStatus(),
                false, reason, null);
    }

    private void publishEvent(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum eventType,
                              DeltaOrderMarketListingDO listing,
                              Long claimedClubId, Long claimedClubTenantId) {
        try {
            DeltaEventPublishReq req = DeltaEventPublishReq.builder()
                    .eventType(eventType.getType())
                    .aggregateType(MARKET_AGGREGATE_TYPE)
                    .aggregateId(listing.getId())
                    .tenantId(listing.getSourceTenantId())
                    .bizKey("market:" + listing.getListingNo() + ":" + eventType.getType())
                    .build();
            // 如果是 CLAIMED/ASSIGNED，设置收件人相关
            if (claimedClubId != null) {
                req.setRecipientId(claimedClubId);
            }
            deltaEventPublisher.publishToAdmin(req);
        } catch (Exception e) {
            log.error("发布Outbox事件失败 eventType={}, listingNo={}", eventType.getType(), listing.getListingNo(), e);
        }
    }
}
