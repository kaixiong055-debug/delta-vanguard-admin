package cn.iocoder.yudao.module.delta.service.market;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 订单市场资格校验服务
 * <p>
 * 统一校验俱乐部是否满足接单条件，平台指定和俱乐部抢单复用同一套校验逻辑。
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaOrderMarketEligibilityService {

    @Resource
    private DeltaClubProfileMapper deltaClubProfileMapper;

    @Resource
    private DeltaClubServiceScopeMapper deltaClubServiceScopeMapper;

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;

    @Resource
    private DeltaOrderMarketListingMapper deltaOrderMarketListingMapper;

    /**
     * 根据可信的当前会员 ID 获取其拥有且启用的俱乐部。
     */
    public DeltaClubProfileDO getAndValidateClubByOwnerMemberId(Long memberUserId) {
        DeltaClubProfileDO club = TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectByOwnerMemberId(memberUserId));
        if (club == null) {
            throw exception(ORDER_MARKET_CLUB_NOT_EXISTS);
        }
        if (club.getBusinessStatus() == null || club.getBusinessStatus() != 1) {
            throw exception(ORDER_MARKET_CLUB_DISABLED);
        }
        return club;
    }

    /**
     * 校验俱乐部是否满足接取指定挂牌的条件（平台 assign 和俱乐部 claim 共用）
     *
     * @param listing   挂牌
     * @param clubId    俱乐部档案ID
     * @param clubTenantId 俱乐部租户ID
     */
    public void checkEligibility(DeltaOrderMarketListingDO listing, Long clubId, Long clubTenantId) {
        // 1. 挂牌状态检查
        if (listing == null) {
            throw exception(ORDER_MARKET_LISTING_NOT_EXISTS);
        }
        if (!DeltaOrderMarketStatusEnum.AVAILABLE.getStatus().equals(listing.getListingStatus())) {
            throw exception(ORDER_MARKET_STATUS_NOT_ALLOWED);
        }
        if (listing.getExpireTime() != null && listing.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            throw exception(ORDER_MARKET_EXPIRED);
        }

        // 2. 服务订单检查（使用枚举判断终态，禁止硬编码数字）
        DeltaServiceOrderDO order = TenantUtils.executeIgnore(() ->
                deltaServiceOrderMapper.selectById(listing.getServiceOrderId()));
        if (order == null) {
            throw exception(ORDER_MARKET_ORDER_NOT_EXISTS);
        }
        // 服务订单不能是终态：已完成(80)、售后中(90)、纠纷中(100)、已取消(110)
        if (ServiceOrderStatusEnum.isCompleted(order.getStatus())
                || ServiceOrderStatusEnum.isCanceled(order.getStatus())
                || ServiceOrderStatusEnum.isAfterSaleOrDispute(order.getStatus())) {
            throw exception(ORDER_MARKET_ORDER_STATUS_NOT_ALLOWED);
        }

        // 3. 俱乐部检查
        DeltaClubProfileDO club = TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectById(clubId));
        if (club == null) {
            throw exception(ORDER_MARKET_CLUB_NOT_EXISTS);
        }
        if (club.getBusinessStatus() == null || club.getBusinessStatus() != 1) {
            throw exception(ORDER_MARKET_CLUB_DISABLED);
        }

        // 4. 服务范围匹配
        List<DeltaClubServiceScopeDO> scopes = TenantUtils.executeIgnore(() ->
                deltaClubServiceScopeMapper.selectListByClubProfileId(clubId));
        Set<Integer> enabledServiceTypes = scopes.stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled())
                .map(DeltaClubServiceScopeDO::getServiceType)
                .collect(Collectors.toSet());
        if (!enabledServiceTypes.contains(listing.getServiceType())) {
            throw exception(ORDER_MARKET_SERVICE_SCOPE_NOT_MATCH);
        }

        // 5. 最大并发订单数检查
        // 只统计 claimedClubTenantId = 当前俱乐部 且 listingStatus = CLAIMED
        // 且对应服务订单处于真实非终态（排除已完成/已取消/售后/纠纷）
        long claimedCount = countNonTerminalClaimed(clubId, clubTenantId);
        int maxOrders = club.getMaxConcurrentOrders() != null ? club.getMaxConcurrentOrders() : 100;
        if (claimedCount >= maxOrders) {
            throw exception(ORDER_MARKET_CLUB_CAPACITY_FULL);
        }
    }

    /**
     * 统计俱乐部当前非终态 CLAIMED 的挂牌数
     * <p>
     * 跨租户逐条查询服务订单状态，筛选出非终态的订单。
     */
    private long countNonTerminalClaimed(Long clubId, Long clubTenantId) {
        List<DeltaOrderMarketListingDO> claimedListings = deltaOrderMarketListingMapper
                .selectClaimedByClub(clubId, clubTenantId);
        if (claimedListings.isEmpty()) {
            return 0L;
        }
        long nonTerminalCount = 0L;
        for (DeltaOrderMarketListingDO listing : claimedListings) {
            DeltaServiceOrderDO order = TenantUtils.executeIgnore(() ->
                    deltaServiceOrderMapper.selectById(listing.getServiceOrderId()));
            if (order != null
                    && !ServiceOrderStatusEnum.isCompleted(order.getStatus())
                    && !ServiceOrderStatusEnum.isCanceled(order.getStatus())
                    && !ServiceOrderStatusEnum.isAfterSaleOrDispute(order.getStatus())) {
                nonTerminalCount++;
            }
        }
        return nonTerminalCount;
    }

    /**
     * 锁内重新校验服务订单状态（轻量级，只查订单状态）
     */
    public void recheckOrderStatus(Long serviceOrderId) {
        DeltaServiceOrderDO order = TenantUtils.executeIgnore(() ->
                deltaServiceOrderMapper.selectById(serviceOrderId));
        if (order == null) {
            throw exception(ORDER_MARKET_ORDER_NOT_EXISTS);
        }
        if (ServiceOrderStatusEnum.isCompleted(order.getStatus())
                || ServiceOrderStatusEnum.isCanceled(order.getStatus())
                || ServiceOrderStatusEnum.isAfterSaleOrDispute(order.getStatus())) {
            throw exception(ORDER_MARKET_ORDER_STATUS_NOT_ALLOWED);
        }
    }

    /**
     * 锁内重新校验俱乐部容量
     */
    public void recheckClubCapacity(Long clubId, Long clubTenantId, Integer maxConcurrentOrders) {
        DeltaClubProfileDO club = TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectById(clubId));
        if (club == null || club.getBusinessStatus() == null || club.getBusinessStatus() != 1) {
            throw exception(ORDER_MARKET_CLUB_DISABLED);
        }
        long claimedCount = countNonTerminalClaimed(clubId, clubTenantId);
        int maxOrders = maxConcurrentOrders != null ? maxConcurrentOrders : 100;
        if (claimedCount >= maxOrders) {
            throw exception(ORDER_MARKET_CLUB_CAPACITY_FULL);
        }
    }

    /**
     * 校验当前租户对应的俱乐部（返回俱乐部信息，用于抢单场景）
     *
     * @return 俱乐部档案，如果不存在则抛异常
     */
    public DeltaClubProfileDO getAndValidateCurrentClub() {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        DeltaClubProfileDO club = TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectByTenantId(tenantId));
        if (club == null) {
            throw exception(ORDER_MARKET_CLUB_NOT_EXISTS);
        }
        if (club.getBusinessStatus() == null || club.getBusinessStatus() != 1) {
            throw exception(ORDER_MARKET_CLUB_DISABLED);
        }
        return club;
    }
}
