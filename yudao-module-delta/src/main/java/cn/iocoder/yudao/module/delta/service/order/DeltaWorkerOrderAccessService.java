package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 打手订单统一访问服务，兼容同租户订单与俱乐部跨租户订单。
 */
@Service
public class DeltaWorkerOrderAccessService {

    @Resource
    private DeltaWorkerService workerService;
    @Resource
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Resource
    private DeltaClubProfileMapper clubProfileMapper;
    @Resource
    private DeltaClubOrderTenantResolver clubOrderTenantResolver;

    public DeltaWorkerDO getCurrentWorker(Long loginUserId) {
        Long currentTenantId = TenantContextHolder.getRequiredTenantId();
        DeltaWorkerDO worker = workerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        if (!Objects.equals(worker.getTenantId(), currentTenantId)) {
            throw exception(CLUB_ORDER_WORKER_TENANT_MISMATCH);
        }
        if (!WorkerAuditStatusEnum.isApproved(worker.getAuditStatus())) {
            throw exception(WORKER_NOT_APPROVED);
        }
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            throw exception(WORKER_DISABLED);
        }
        return worker;
    }

    public PageResult<DeltaServiceOrderDO> getAccessiblePage(Long loginUserId, Integer status,
                                                              PageParam pageParam) {
        DeltaWorkerDO worker = getCurrentWorker(loginUserId);
        DeltaClubProfileDO club = clubProfileMapper.selectByTenantId(worker.getTenantId());
        Long clubId = club != null && Objects.equals(club.getTenantId(), worker.getTenantId())
                && DeltaClubBusinessStatusEnum.isEnabled(club.getBusinessStatus()) ? club.getId() : null;
        return TenantUtils.executeIgnore(() -> serviceOrderMapper.selectWorkerAccessiblePage(
                pageParam, worker.getId(), worker.getTenantId(), clubId, status));
    }

    public DeltaWorkerOrderAccessContext resolve(Long loginUserId, Long serviceOrderId) {
        DeltaWorkerDO currentWorker = getCurrentWorker(loginUserId);
        DeltaServiceOrderDO localOrder = serviceOrderMapper.selectById(serviceOrderId);
        if (localOrder != null) {
            validateLocalOrder(localOrder, currentWorker);
            return buildLocalContext(currentWorker, localOrder);
        }

        DeltaClubOrderTenantContext clubContext = clubOrderTenantResolver.resolve(serviceOrderId);
        validateCurrentWorker(currentWorker, clubContext);
        return buildClubContext(clubContext);
    }

    /** 事务内重新验证订单、挂牌、派单与打手归属。 */
    public DeltaWorkerOrderAccessContext revalidate(DeltaWorkerOrderAccessContext context) {
        if (context.isClubOrder()) {
            DeltaClubOrderTenantContext clubContext = clubOrderTenantResolver.resolve(context.getOrder().getId());
            if (!Objects.equals(clubContext.getWorker().getId(), context.getWorker().getId())
                    || !Objects.equals(clubContext.getWorkerTenantId(), context.getWorkerTenantId())) {
                throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
            }
            return buildClubContext(clubContext);
        }
        DeltaServiceOrderDO order = serviceOrderMapper.selectById(context.getOrder().getId());
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        validateLocalOrder(order, context.getWorker());
        return buildLocalContext(context.getWorker(), order);
    }

    private void validateLocalOrder(DeltaServiceOrderDO order, DeltaWorkerDO worker) {
        if (!Objects.equals(order.getTenantId(), worker.getTenantId())) {
            throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
        }
        if (!Objects.equals(order.getAssignedWorkerId(), worker.getId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
    }

    private void validateCurrentWorker(DeltaWorkerDO currentWorker,
                                       DeltaClubOrderTenantContext clubContext) {
        if (!Objects.equals(clubContext.getWorker().getId(), currentWorker.getId())
                || !Objects.equals(clubContext.getWorker().getUserId(), currentWorker.getUserId())
                || !Objects.equals(clubContext.getWorkerTenantId(), currentWorker.getTenantId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
    }

    private DeltaWorkerOrderAccessContext buildLocalContext(DeltaWorkerDO worker,
                                                             DeltaServiceOrderDO order) {
        return DeltaWorkerOrderAccessContext.builder().worker(worker)
                .workerTenantId(worker.getTenantId()).order(order)
                .sourceTenantId(order.getTenantId()).clubOrder(false).build();
    }

    private DeltaWorkerOrderAccessContext buildClubContext(DeltaClubOrderTenantContext context) {
        return DeltaWorkerOrderAccessContext.builder().worker(context.getWorker())
                .workerTenantId(context.getWorkerTenantId()).club(context.getClub())
                .listing(context.getListing()).assignment(context.getAssignment())
                .order(context.getOrder()).sourceTenantId(context.getSourceTenantId())
                .clubOrder(true).build();
    }
}
