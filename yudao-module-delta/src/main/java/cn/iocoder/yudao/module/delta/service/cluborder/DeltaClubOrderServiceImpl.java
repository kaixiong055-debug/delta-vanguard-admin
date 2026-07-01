package cn.iocoder.yudao.module.delta.service.cluborder;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAssignWorkerReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAvailableWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;
import cn.iocoder.yudao.module.delta.convert.cluborder.AppDeltaClubOrderConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaServiceOrderLockRedisDAO;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.service.market.DeltaOrderMarketEligibilityService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

@Service
@Validated
public class DeltaClubOrderServiceImpl implements DeltaClubOrderService {

    @Resource
    private DeltaOrderMarketEligibilityService eligibilityService;
    @Resource
    private DeltaOrderMarketListingMapper listingMapper;
    @Resource
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Resource
    private DeltaWorkerMapper workerMapper;
    @Resource
    private DeltaServiceOrderLockRedisDAO serviceOrderLockRedisDAO;
    @Resource
    private DeltaClubOrderAssignmentCoreService assignmentCoreService;

    @Override
    public PageResult<AppDeltaClubOrderPageRespVO> getPageForMember(
            Long memberUserId, AppDeltaClubOrderPageReqVO reqVO) {
        DeltaClubProfileDO club = eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        PageResult<DeltaOrderMarketListingDO> page = listingMapper.selectClaimedPage(
                reqVO.getPageNo(), reqVO.getPageSize(), club.getId(), club.getTenantId());
        if (page.getList().isEmpty()) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, DeltaServiceOrderDO> orderMap = batchLoadAndValidateOrders(page.getList(), club);
        Map<Long, DeltaWorkerDO> workerMap = batchLoadWorkers(
                orderMap.values(), club.getTenantId());
        List<AppDeltaClubOrderPageRespVO> result = new ArrayList<>(page.getList().size());
        for (DeltaOrderMarketListingDO listing : page.getList()) {
            DeltaServiceOrderDO order = orderMap.get(listing.getServiceOrderId());
            result.add(AppDeltaClubOrderConvert.INSTANCE.convertPageItem(
                    listing, order, workerMap.get(order.getAssignedWorkerId())));
        }
        return new PageResult<>(result, page.getTotal());
    }

    @Override
    public AppDeltaClubOrderDetailRespVO getDetailForMember(Long memberUserId, Long listingId) {
        OwnedOrder context = getOwnedOrder(memberUserId, listingId);
        DeltaWorkerDO worker = loadWorker(context.order.getAssignedWorkerId(), context.club.getTenantId());
        return AppDeltaClubOrderConvert.INSTANCE.convertDetail(
                context.listing, context.order, worker);
    }

    @Override
    public PageResult<AppDeltaClubOrderWorkerRespVO> getAvailableWorkerPageForMember(
            Long memberUserId, AppDeltaClubOrderAvailableWorkerPageReqVO reqVO) {
        OwnedOrder context = getOwnedOrder(memberUserId, reqVO.getListingId());
        Set<Long> busyWorkerIds = TenantUtils.executeIgnore(
                () -> serviceOrderMapper.selectActiveAssignedWorkerIds());
        PageResult<DeltaWorkerDO> page = TenantUtils.execute(context.club.getTenantId(), () ->
                workerMapper.selectClubAvailablePage(reqVO, context.club.getTenantId(),
                        context.order.getServiceType(), context.order.getDeviceType(), busyWorkerIds));
        List<AppDeltaClubOrderWorkerRespVO> result = page.getList().stream()
                .map(worker -> {
                    if (!Objects.equals(worker.getTenantId(), context.club.getTenantId())) {
                        throw exception(CLUB_ORDER_WORKER_TENANT_MISMATCH);
                    }
                    return AppDeltaClubOrderConvert.INSTANCE.convertWorker(worker);
                }).collect(Collectors.toList());
        return new PageResult<>(result, page.getTotal());
    }

    @Override
    public void assignWorkerForMember(Long memberUserId, AppDeltaClubOrderAssignWorkerReqVO reqVO) {
        OwnedOrder context = getOwnedOrder(memberUserId, reqVO.getListingId());
        try {
            serviceOrderLockRedisDAO.lockAndRun(
                    context.listing.getSourceTenantId(), context.listing.getServiceOrderId(), () -> {
                        assignmentCoreService.assign(context.listing.getId(), context.club.getId(),
                                context.club.getTenantId(), memberUserId, reqVO.getWorkerId(),
                                reqVO.getRemark());
                        return null;
                    });
        } catch (ServiceException ex) {
            if (Objects.equals(ex.getCode(), ASSIGNMENT_ORDER_BEING_PROCESSED.getCode())) {
                throw exception(CLUB_ORDER_ASSIGN_BUSY);
            }
            throw ex;
        }
    }

    private OwnedOrder getOwnedOrder(Long memberUserId, Long listingId) {
        DeltaClubProfileDO club = eligibilityService.getAndValidateClubByOwnerMemberId(memberUserId);
        DeltaOrderMarketListingDO listing = TenantUtils.executeIgnore(() -> listingMapper.selectById(listingId));
        validateListingOwnership(listing, club);
        DeltaServiceOrderDO order = loadAndValidateOrder(listing);
        return new OwnedOrder(club, listing, order);
    }

    private void validateListingOwnership(DeltaOrderMarketListingDO listing, DeltaClubProfileDO club) {
        if (listing == null) {
            throw exception(CLUB_ORDER_LISTING_NOT_EXISTS);
        }
        if (!Objects.equals(listing.getClaimedClubId(), club.getId())
                || !Objects.equals(listing.getClaimedClubTenantId(), club.getTenantId())) {
            throw exception(CLUB_ORDER_NOT_BELONG_TO_CLUB);
        }
        if (!DeltaOrderMarketStatusEnum.CLAIMED.getStatus().equals(listing.getListingStatus())) {
            throw exception(CLUB_ORDER_LISTING_STATUS_INVALID);
        }
        if (listing.getServiceOrderId() == null || listing.getSourceTenantId() == null) {
            throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
        }
    }

    private Map<Long, DeltaServiceOrderDO> batchLoadAndValidateOrders(
            List<DeltaOrderMarketListingDO> listings, DeltaClubProfileDO club) {
        for (DeltaOrderMarketListingDO listing : listings) {
            validateListingOwnership(listing, club);
        }
        Map<Long, List<DeltaOrderMarketListingDO>> groups = listings.stream()
                .collect(Collectors.groupingBy(DeltaOrderMarketListingDO::getSourceTenantId));
        Map<Long, DeltaServiceOrderDO> result = new HashMap<>();
        for (Map.Entry<Long, List<DeltaOrderMarketListingDO>> entry : groups.entrySet()) {
            Long sourceTenantId = entry.getKey();
            if (sourceTenantId == null) {
                throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
            }
            List<Long> ids = entry.getValue().stream()
                    .map(DeltaOrderMarketListingDO::getServiceOrderId).collect(Collectors.toList());
            List<DeltaServiceOrderDO> orders = TenantUtils.execute(sourceTenantId,
                    () -> serviceOrderMapper.selectBatchIds(ids));
            Map<Long, DeltaServiceOrderDO> tenantOrders = orders.stream()
                    .collect(Collectors.toMap(DeltaServiceOrderDO::getId, Function.identity()));
            for (DeltaOrderMarketListingDO listing : entry.getValue()) {
                DeltaServiceOrderDO order = tenantOrders.get(listing.getServiceOrderId());
                if (order == null) {
                    detectOrderTenantMismatch(listing);
                    throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
                }
                validateOrderTenant(order, listing.getSourceTenantId());
                result.put(order.getId(), order);
            }
        }
        return result;
    }

    private DeltaServiceOrderDO loadAndValidateOrder(DeltaOrderMarketListingDO listing) {
        DeltaServiceOrderDO order = TenantUtils.execute(listing.getSourceTenantId(),
                () -> serviceOrderMapper.selectById(listing.getServiceOrderId()));
        if (order == null) {
            detectOrderTenantMismatch(listing);
            throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
        }
        validateOrderTenant(order, listing.getSourceTenantId());
        return order;
    }

    private void detectOrderTenantMismatch(DeltaOrderMarketListingDO listing) {
        DeltaServiceOrderDO actual = TenantUtils.executeIgnore(
                () -> serviceOrderMapper.selectById(listing.getServiceOrderId()));
        if (actual != null && !Objects.equals(actual.getTenantId(), listing.getSourceTenantId())) {
            throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
        }
    }

    private void validateOrderTenant(DeltaServiceOrderDO order, Long sourceTenantId) {
        if (!Objects.equals(order.getTenantId(), sourceTenantId)) {
            throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
        }
    }

    private Map<Long, DeltaWorkerDO> batchLoadWorkers(Collection<DeltaServiceOrderDO> orders,
                                                       Long clubTenantId) {
        List<Long> workerIds = orders.stream().map(DeltaServiceOrderDO::getAssignedWorkerId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (workerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DeltaWorkerDO> workers = TenantUtils.execute(clubTenantId,
                () -> workerMapper.selectBatchIds(workerIds));
        return workers.stream().filter(worker -> Objects.equals(worker.getTenantId(), clubTenantId))
                .collect(Collectors.toMap(DeltaWorkerDO::getId, Function.identity()));
    }

    private DeltaWorkerDO loadWorker(Long workerId, Long clubTenantId) {
        if (workerId == null) {
            return null;
        }
        DeltaWorkerDO worker = TenantUtils.execute(clubTenantId,
                () -> workerMapper.selectById(workerId));
        return worker != null && Objects.equals(worker.getTenantId(), clubTenantId) ? worker : null;
    }

    private static final class OwnedOrder {
        private final DeltaClubProfileDO club;
        private final DeltaOrderMarketListingDO listing;
        private final DeltaServiceOrderDO order;

        private OwnedOrder(DeltaClubProfileDO club, DeltaOrderMarketListingDO listing,
                           DeltaServiceOrderDO order) {
            this.club = club;
            this.listing = listing;
            this.order = order;
        }
    }
}
