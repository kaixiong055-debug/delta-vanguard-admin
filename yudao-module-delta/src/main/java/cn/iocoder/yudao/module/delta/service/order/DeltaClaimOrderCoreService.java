package cn.iocoder.yudao.module.delta.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerSkillMapper;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 接单核心事务 Service（独立 Bean 确保 @Transactional 生效）
 * <p>
 * 不能放在 DeltaServiceOrderServiceImpl 内部由 this 调用，
 * 否则 Spring AOP 代理无法拦截，事务不生效。
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaClaimOrderCoreService {

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaWorkerService deltaWorkerService;
    @Resource
    private DeltaWorkerSkillMapper deltaWorkerSkillMapper;
    @Resource
    private DeltaOrderAssignmentService deltaOrderAssignmentService;
    @Resource
    private DeltaOrderLogService deltaOrderLogService;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;

    /**
     * 打手接单核心事务（必须在 Redisson 锁内 + Spring 事务内执行）
     *
     * @param worker         打手对象（锁外已查）
     * @param serviceOrderId 服务单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void doClaimOrder(DeltaWorkerDO worker, Long serviceOrderId) {
        // 1. 重新查询服务单（锁内最新数据）
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.POOL_PENDING.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_ALREADY_CLAIMED);
        }
        if (order.getAssignedWorkerId() != null) {
            throw exception(SERVICE_ORDER_ALREADY_CLAIMED);
        }

        // 2. 锁内重新校验打手状态
        DeltaWorkerDO currentWorker = deltaWorkerService.getWorker(worker.getId());
        validateWorkerForClaim(currentWorker);
        validateWorkerSkill(currentWorker, order.getDeviceType(), order.getServiceType());

        // 3. 校验没有有效分配
        DeltaOrderAssignmentDO existAssignment = deltaOrderAssignmentService.getActiveAssignmentByServiceOrderId(serviceOrderId);
        if (existAssignment != null) {
            throw exception(ASSIGNMENT_ALREADY_EXISTS);
        }
        List<DeltaOrderAssignmentDO> workerAssignments = deltaOrderAssignmentService.getActiveAssignmentsByWorkerId(worker.getId());
        if (CollUtil.isNotEmpty(workerAssignments)) {
            throw exception(ASSIGNMENT_WORKER_HAS_ACTIVE_ORDER);
        }

        // 4. CAS 更新服务单状态 + assignedWorkerId
        Integer oldStatus = order.getStatus();
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                oldStatus,
                wrapper -> wrapper.set(DeltaServiceOrderDO::getAssignedWorkerId, worker.getId())
        );
        if (rows != 1) {
            throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
        }

        // 5. 创建分配记录
        DeltaOrderAssignmentDO assignment = new DeltaOrderAssignmentDO();
        assignment.setServiceOrderId(serviceOrderId);
        assignment.setWorkerId(worker.getId());
        assignment.setAssignmentType(AssignmentTypeEnum.PUBLIC_CLAIM.getType());
        assignment.setAssignmentStatus(AssignmentStatusEnum.ACCEPTED.getStatus());
        assignment.setOperatorType(OperatorTypeEnum.WORKER.getType());
        assignment.setOperatorId(worker.getId());
        deltaOrderAssignmentService.createAssignment(assignment);

        // 6. CAS 打手状态 ONLINE → BUSY
        int wRows = deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                worker.getId(),
                WorkerWorkStatusEnum.BUSY.getStatus(),
                WorkerWorkStatusEnum.ONLINE.getStatus()
        );
        if (wRows != 1) {
            throw new RuntimeException("打手状态已变化，接单失败");
        }

        // 7. 写操作日志
        DeltaOrderLogDO logEntry = new DeltaOrderLogDO();
        logEntry.setServiceOrderId(serviceOrderId);
        logEntry.setOperatorType(OperatorTypeEnum.WORKER.getType());
        logEntry.setOperatorId(worker.getId());
        logEntry.setOperation("打手接单");
        logEntry.setBeforeStatus(oldStatus);
        logEntry.setAfterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus());
        logEntry.setContent("打手ID=" + worker.getId() + "(" + worker.getDisplayName() + ") 接单");
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("接单成功 serviceOrderId={}, workerId={}", serviceOrderId, worker.getId());

        // Phase 10: 发布接单成功事件
        try {
            Long workerUserId = currentWorker.getUserId();
            deltaEventPublisher.publishToWorker(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_ORDER_CLAIMED.getType())
                    .tenantId(order.getTenantId())
                    .aggregateType("SERVICE_ORDER")
                    .aggregateId(serviceOrderId)
                    .bizKey("SERVICE_ORDER_CLAIMED:" + serviceOrderId + ":" + assignment.getId() + ":" + workerUserId)
                    .recipientId(workerUserId)
                    .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.ORDER_CLAIMED.getCode())
                    .templateParams(java.util.Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                    .build());
        } catch (Exception e) {
            log.error("接单事件写入Outbox失败 serviceOrderId={}", serviceOrderId, e);
        }
    }

    // ====== 内部校验 ======

    private void validateWorkerForClaim(DeltaWorkerDO worker) {
        if (worker == null) {
            throw exception(ASSIGNMENT_WORKER_NOT_AVAILABLE);
        }
        if (!WorkerAuditStatusEnum.isApproved(worker.getAuditStatus())) {
            throw exception(WORKER_NOT_APPROVED);
        }
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            throw exception(WORKER_DISABLED);
        }
        if (!WorkerWorkStatusEnum.ONLINE.getStatus().equals(worker.getWorkStatus())) {
            if (WorkerWorkStatusEnum.BUSY.getStatus().equals(worker.getWorkStatus())) {
                throw exception(ASSIGNMENT_WORKER_BUSY);
            }
            if (WorkerWorkStatusEnum.PAUSED.getStatus().equals(worker.getWorkStatus())) {
                throw exception(ASSIGNMENT_WORKER_PAUSED);
            }
            throw exception(ASSIGNMENT_WORKER_NOT_ONLINE);
        }
    }

    private void validateWorkerSkill(DeltaWorkerDO worker, Integer deviceType, Integer serviceType) {
        if (!deltaWorkerSkillMapper.hasMatchingSkill(worker.getId(), deviceType, serviceType)) {
            throw exception(ASSIGNMENT_WORKER_SKILL_NOT_MATCH);
        }
    }
}
