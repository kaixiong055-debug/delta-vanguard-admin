package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.enums.order.*;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementPhase7CoreService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Phase 6 验收与返工核心事务 Service（独立 Bean 确保 @Transactional 生效）
 * <p>
 * 不能放在 DeltaServiceOrderServiceImpl 内部由 this 调用，
 * 否则 Spring AOP 代理无法拦截，事务不生效。
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaServiceOrderPhase6CoreService {

    /**
     * 最大返工次数
     */
    public static final int MAX_REWORK_COUNT = 3;

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaWorkerService deltaWorkerService;
    @Resource
    private DeltaOrderAssignmentService deltaOrderAssignmentService;
    @Resource
    private DeltaOrderLogService deltaOrderLogService;
    @Resource
    private DeltaOrderProgressService deltaOrderProgressService;
    @Resource
    private DeltaOrderEvidenceService deltaOrderEvidenceService;
    @Resource
    private DeltaOrderAcceptanceService deltaOrderAcceptanceService;
    @Resource
    private DeltaOrderReworkService deltaOrderReworkService;
    @Resource
    private DeltaWorkerSettlementPhase7CoreService deltaWorkerSettlementPhase7CoreService;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;
    @Resource
    private DeltaClubOrderTenantResolver deltaClubOrderTenantResolver;

    // ========== 老板验收通过 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doAcceptByBuyer(Long buyerUserId, Long serviceOrderId, String remark) {
        doAccept(buyerUserId, serviceOrderId, OperatorTypeEnum.CUSTOMER.getType(), remark);
    }

    // ========== 老板要求返工 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doRequestReworkByBuyer(Long buyerUserId, Long serviceOrderId, String reason) {
        doRequestRework(buyerUserId, serviceOrderId, OperatorTypeEnum.CUSTOMER.getType(), reason);
    }

    // ========== 后台验收通过 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doAcceptByAdmin(Long adminUserId, Long serviceOrderId, String remark) {
        doAccept(adminUserId, serviceOrderId, OperatorTypeEnum.ADMIN.getType(), remark);
    }

    // ========== 后台要求返工 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doRequestReworkByAdmin(Long adminUserId, Long serviceOrderId, String reason) {
        doRequestRework(adminUserId, serviceOrderId, OperatorTypeEnum.ADMIN.getType(), reason);
    }

    // ========== 核心验收逻辑 ==========

    private void doAccept(Long operatorId, Long serviceOrderId, Integer operatorType, String remark) {
        // 1. 重新查询服务单（事务内最新数据）
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }

        Integer oldStatus = order.getStatus();
        Long workerId = order.getAssignedWorkerId();

        // 2. 校验状态：必须是 WORKER_SUBMITTED
        if (!ServiceOrderStatusEnum.isAcceptable(oldStatus)) {
            if (ServiceOrderStatusEnum.isCompleted(oldStatus)) {
                throw exception(SERVICE_ORDER_ALREADY_ACCEPTED);
            }
            throw exception(SERVICE_ORDER_STATUS_CANNOT_ACCEPT);
        }

        // 3. 校验有效分配记录
        DeltaOrderAssignmentDO assignment = deltaOrderAssignmentService.getActiveAssignmentByServiceOrderId(serviceOrderId);
        if (assignment == null) {
            throw exception(ACCEPTANCE_NO_VALID_ASSIGNMENT);
        }

        // 4. 校验至少有一条有效凭证（复验时可能有更多凭证）
        List<DeltaOrderEvidenceDO> evidences = deltaOrderEvidenceService.getEvidenceListByServiceOrderId(serviceOrderId);
        if (evidences.isEmpty()) {
            throw exception(EVIDENCE_NO_COMPLETION_EVIDENCE);
        }

        WorkerTarget workerTarget = resolveWorkerTarget(order);

        // 5. CAS 更新服务单：WORKER_SUBMITTED -> COMPLETED
        LocalDateTime now = LocalDateTime.now();
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.COMPLETED.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaServiceOrderDO::getVerifiedAt, now)
                        .set(DeltaServiceOrderDO::getCompletedAt, now)
        );
        if (rows != 1) {
            throw exception(SERVICE_ORDER_STATUS_CHANGED);
        }

        // 6. 创建验收记录
        DeltaOrderAcceptanceDO acceptance = DeltaOrderAcceptanceDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(workerId)
                .acceptanceResult(AcceptanceResultEnum.PASS.getResult())
                .operatorType(operatorType)
                .operatorId(operatorId)
                .remark(remark)
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.COMPLETED.getStatus())
                .acceptanceTime(now)
                .build();
        deltaOrderAcceptanceService.createAcceptance(acceptance);

        // 7. 创建系统进度（验收通过）
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(workerId)
                .progressType(ProgressTypeEnum.ACCEPTANCE_PASSED.getType())
                .progressPercent(100)
                .content("验收通过" + (remark != null ? "：" + remark : ""))
                .build();
        deltaOrderProgressService.createProgress(progress);

        // 8. 写订单日志
        String operatorName = OperatorTypeEnum.CUSTOMER.getType().equals(operatorType) ? "老板" : "管理员";
        DeltaOrderLogDO logEntry = DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .operation("验收通过")
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.COMPLETED.getStatus())
                .content(operatorName + "ID=" + operatorId + " 验收通过" + (remark != null ? "，备注: " + remark : ""))
                .build();
        deltaOrderLogService.createOrderLog(logEntry);

        // 9. 释放打手忙碌状态（如果没有其他有效订单）
        releaseWorkerStatusIfNoActiveOrders(order, workerTarget);

        // 10. Phase 7: 生成打手结算单（待审核，同一事务内）
        deltaWorkerSettlementPhase7CoreService.createSettlementForCompletedOrder(serviceOrderId);

        log.info("验收通过成功 serviceOrderId={}, operatorType={}, operatorId={}", serviceOrderId, operatorType, operatorId);

        // Phase 10: 发布验收完成事件 -> 通知打手
        try {
            Long workerUserId = workerTarget.worker.getUserId();
            String orderNo = order.getServiceOrderNo();
            if (workerUserId != null) {
                TenantUtils.execute(workerTarget.tenantId, () -> deltaEventPublisher.publishToWorker(
                        cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                                .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_ACCEPTED.getType())
                                .tenantId(workerTarget.tenantId)
                                .aggregateType("SERVICE_ORDER")
                                .aggregateId(serviceOrderId)
                                .bizKey("SERVICE_ACCEPTED:" + serviceOrderId + ":" + workerId)
                                .recipientId(workerUserId)
                                .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SERVICE_ACCEPTED.getCode())
                                .templateParams(java.util.Collections.singletonMap("orderNo", orderNo))
                                .build()));
            }
        } catch (Exception e) {
            log.error("验收完成事件写入Outbox失败 serviceOrderId={}", serviceOrderId, e);
        }
    }

    // ========== 核心返工逻辑 ==========

    private void doRequestRework(Long operatorId, Long serviceOrderId, Integer operatorType, String reason) {
        // 1. 重新查询服务单（事务内最新数据）
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }

        Integer oldStatus = order.getStatus();
        Long workerId = order.getAssignedWorkerId();

        // 2. 校验状态：必须是 WORKER_SUBMITTED
        if (!ServiceOrderStatusEnum.isReworkable(oldStatus)) {
            if (ServiceOrderStatusEnum.isCompleted(oldStatus)) {
                throw exception(SERVICE_ORDER_STATUS_CANNOT_REWORK);
            }
            throw exception(SERVICE_ORDER_STATUS_CANNOT_REWORK);
        }

        // 3. 校验返工次数
        long reworkCount = deltaOrderReworkService.countReworkByServiceOrderId(serviceOrderId);
        if (reworkCount >= MAX_REWORK_COUNT) {
            throw exception(REWORK_COUNT_EXCEED);
        }

        WorkerTarget workerTarget = resolveWorkerTarget(order);

        // 4. CAS 更新服务单：WORKER_SUBMITTED -> IN_PROGRESS
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                oldStatus,
                wrapper -> {}
        );
        if (rows != 1) {
            throw exception(SERVICE_ORDER_STATUS_CHANGED);
        }

        int currentReworkNo = (int) (reworkCount + 1);

        // 5. 创建返工记录
        DeltaOrderReworkDO rework = DeltaOrderReworkDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(workerId)
                .reworkNo(currentReworkNo)
                .reason(reason)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.IN_PROGRESS.getStatus())
                .build();
        deltaOrderReworkService.createRework(rework);

        // 6. 创建系统进度（要求返工）
        String operatorName = OperatorTypeEnum.CUSTOMER.getType().equals(operatorType) ? "老板" : "管理员";
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(workerId)
                .progressType(ProgressTypeEnum.REWORK_REQUEST.getType())
                .progressPercent(0)
                .content("第" + currentReworkNo + "次返工，" + operatorName + "要求: " + reason)
                .build();
        deltaOrderProgressService.createProgress(progress);

        // 7. 写订单日志
        DeltaOrderLogDO logEntry = DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .operation("要求返工")
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.IN_PROGRESS.getStatus())
                .content(operatorName + "ID=" + operatorId + " 要求第" + currentReworkNo + "次返工，原因: " + reason)
                .build();
        deltaOrderLogService.createOrderLog(logEntry);

        // 8. 打手继续保持忙碌（不释放）

        log.info("要求返工成功 serviceOrderId={}, operatorType={}, operatorId={}, reworkNo={}",
                serviceOrderId, operatorType, operatorId, currentReworkNo);

        // Phase 10: 发布返工事件 -> 通知打手
        try {
            Long workerUserId = workerTarget.worker.getUserId();
            if (workerUserId != null) {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("orderNo", order.getServiceOrderNo());
                params.put("reason", reason != null ? reason : "");
                TenantUtils.execute(workerTarget.tenantId, () -> deltaEventPublisher.publishToWorker(
                        cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                                .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_REWORK_REQUESTED.getType())
                                .tenantId(workerTarget.tenantId)
                                .aggregateType("SERVICE_ORDER")
                                .aggregateId(serviceOrderId)
                                .bizKey("SERVICE_REWORK_REQUESTED:" + serviceOrderId + ":" + currentReworkNo)
                                .recipientId(workerUserId)
                                .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SERVICE_REWORK_REQUESTED.getCode())
                                .templateParams(params)
                                .build()));
            }
        } catch (Exception e) {
            log.error("返工事件写入Outbox失败 serviceOrderId={}", serviceOrderId, e);
        }
    }

    // ========== 打手状态释放辅助 ==========

    /**
     * 验收通过后释放打手忙碌状态
     * <p>
     * 如果打手没有其他有效订单（不包括已完成/已取消/售后/纠纷），则将 BUSY 恢复为 ONLINE。
     * 如果打手已被停用，保持 OFFLINE 或当前状态。
     */
    private void releaseWorkerStatusIfNoActiveOrders(DeltaServiceOrderDO completedOrder,
                                                      WorkerTarget workerTarget) {
        Long workerId = completedOrder.getAssignedWorkerId();
        if (workerId == null) {
            return;
        }

        DeltaWorkerDO worker = workerTarget.worker;
        if (worker == null) {
            return;
        }

        // 如果打手已被停用，不恢复在线
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            log.info("验收通过但打手已被停用，不恢复在线 workerId={}", workerId);
            return;
        }

        // 只有 BUSY 状态才需要检查恢复
        if (!WorkerWorkStatusEnum.BUSY.getStatus().equals(worker.getWorkStatus())) {
            log.info("验收通过但打手非BUSY状态，不恢复 workerId={}, workStatus={}", workerId, worker.getWorkStatus());
            return;
        }

        // 服务单跨租户统计，排除当前已完成订单
        boolean hasOtherActiveOrder = TenantUtils.executeIgnore(() ->
                deltaServiceOrderMapper.existsOtherActiveByWorkerId(workerId, completedOrder.getId()));
        if (!hasOtherActiveOrder) {
            // 没有其他有效订单，恢复为 ONLINE
            int rows = TenantUtils.execute(workerTarget.tenantId, () ->
                    deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                            workerId,
                            WorkerWorkStatusEnum.ONLINE.getStatus(),
                            WorkerWorkStatusEnum.BUSY.getStatus()));
            if (rows != 1) {
                throw exception(WORKER_ORDER_WORKER_STATUS_CHANGED);
            }
            log.info("验收通过恢复打手状态 workerId={}, BUSY→ONLINE, affected={}", workerId, rows);
        } else {
            log.info("验收通过但打手仍有其他有效订单，保持BUSY workerId={}", workerId);
        }
    }

    private WorkerTarget resolveWorkerTarget(DeltaServiceOrderDO order) {
        if (DispatchModeEnum.CLUB_ASSIGN.getMode().equals(order.getDispatchMode())) {
            DeltaClubOrderTenantContext context = deltaClubOrderTenantResolver.resolve(order);
            return new WorkerTarget(context.getWorker(), context.getWorkerTenantId());
        }
        DeltaWorkerDO worker = deltaWorkerService.getWorker(order.getAssignedWorkerId());
        if (worker == null || !java.util.Objects.equals(worker.getTenantId(), order.getTenantId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        return new WorkerTarget(worker, order.getTenantId());
    }

    private static final class WorkerTarget {
        private final DeltaWorkerDO worker;
        private final Long tenantId;

        private WorkerTarget(DeltaWorkerDO worker, Long tenantId) {
            this.worker = worker;
            this.tenantId = tenantId;
        }
    }

}
