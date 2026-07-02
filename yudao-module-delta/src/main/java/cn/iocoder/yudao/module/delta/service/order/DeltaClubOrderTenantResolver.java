package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAssignmentMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.DispatchModeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 俱乐部订单租户解析器。可信桥梁固定为 CLAIMED 挂牌，不接受外部租户参数。
 */
@Service
public class DeltaClubOrderTenantResolver {

    @Resource
    private DeltaOrderMarketListingMapper listingMapper;
    @Resource
    private DeltaClubProfileMapper clubProfileMapper;
    @Resource
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Resource
    private DeltaOrderAssignmentMapper assignmentMapper;
    @Resource
    private DeltaWorkerMapper workerMapper;

    public DeltaClubOrderTenantContext resolve(Long serviceOrderId) {
        return resolve(serviceOrderId, null);
    }

    public DeltaClubOrderTenantContext resolve(DeltaServiceOrderDO sourceOrder) {
        if (sourceOrder == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        return resolve(sourceOrder.getId(), sourceOrder);
    }

    private DeltaClubOrderTenantContext resolve(Long serviceOrderId, DeltaServiceOrderDO sourceOrder) {
        DeltaOrderMarketListingDO listing = TenantUtils.executeIgnore(
                () -> listingMapper.selectClaimedByServiceOrderId(serviceOrderId));
        if (listing == null || !Objects.equals(listing.getServiceOrderId(), serviceOrderId)
                || listing.getSourceTenantId() == null || listing.getClaimedClubId() == null
                || listing.getClaimedClubTenantId() == null) {
            throw exception(WORKER_ORDER_CROSS_TENANT_CONTEXT_NOT_FOUND);
        }

        DeltaServiceOrderDO order = sourceOrder;
        if (order == null) {
            order = TenantUtils.execute(listing.getSourceTenantId(),
                    () -> serviceOrderMapper.selectById(serviceOrderId));
            if (order == null) {
                DeltaServiceOrderDO actual = TenantUtils.executeIgnore(
                        () -> serviceOrderMapper.selectById(serviceOrderId));
                if (actual != null) {
                    throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
                }
                throw exception(SERVICE_ORDER_NOT_EXISTS);
            }
        }
        validateOrder(order, listing);

        Long clubTenantId = listing.getClaimedClubTenantId();
        DeltaClubProfileDO club = TenantUtils.execute(clubTenantId,
                () -> clubProfileMapper.selectById(listing.getClaimedClubId()));
        if (club == null || !Objects.equals(club.getId(), listing.getClaimedClubId())
                || !Objects.equals(club.getTenantId(), clubTenantId)
                || !DeltaClubBusinessStatusEnum.isEnabled(club.getBusinessStatus())) {
            throw exception(WORKER_ORDER_CLUB_MISMATCH);
        }

        DeltaWorkerDO worker = TenantUtils.execute(clubTenantId,
                () -> workerMapper.selectById(order.getAssignedWorkerId()));
        if (worker == null || !Objects.equals(worker.getTenantId(), clubTenantId)) {
            throw exception(CLUB_ORDER_WORKER_TENANT_MISMATCH);
        }

        DeltaOrderAssignmentDO assignment = TenantUtils.execute(listing.getSourceTenantId(),
                () -> assignmentMapper.selectActiveByServiceOrderId(serviceOrderId));
        if (assignment == null || !Objects.equals(assignment.getWorkerId(), worker.getId())
                || !AssignmentTypeEnum.CLUB_ASSIGN.getType().equals(assignment.getAssignmentType())) {
            throw exception(WORKER_ORDER_ASSIGNMENT_MISMATCH);
        }

        return DeltaClubOrderTenantContext.builder()
                .listing(listing).club(club).order(order).assignment(assignment).worker(worker)
                .sourceTenantId(listing.getSourceTenantId()).workerTenantId(clubTenantId).build();
    }

    private void validateOrder(DeltaServiceOrderDO order, DeltaOrderMarketListingDO listing) {
        if (!Objects.equals(order.getId(), listing.getServiceOrderId())
                || !Objects.equals(order.getTenantId(), listing.getSourceTenantId())) {
            throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
        }
        if (order.getAssignedWorkerId() == null
                || !DispatchModeEnum.CLUB_ASSIGN.getMode().equals(order.getDispatchMode())) {
            throw exception(WORKER_ORDER_ASSIGNMENT_MISMATCH);
        }
    }
}
