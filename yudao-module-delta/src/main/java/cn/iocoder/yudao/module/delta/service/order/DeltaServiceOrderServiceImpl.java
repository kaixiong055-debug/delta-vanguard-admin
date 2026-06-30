package cn.iocoder.yudao.module.delta.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.DeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.*;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceCreateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressCreateReqVO;
import cn.iocoder.yudao.module.delta.convert.order.DeltaOrderAcceptanceConvert;
import cn.iocoder.yudao.module.delta.convert.order.DeltaOrderReworkConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.*;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerSkillMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaServiceOrderLockRedisDAO;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.EvidenceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ProgressTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 服务履约订单 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaServiceOrderServiceImpl implements DeltaServiceOrderService {

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;
    @Resource
    private DeltaWorkerService deltaWorkerService;
    @Resource
    private DeltaWorkerSkillMapper deltaWorkerSkillMapper;
    @Resource
    private DeltaOrderAssignmentService deltaOrderAssignmentService;
    @Resource
    private DeltaOrderLogService deltaOrderLogService;
    @Resource
    private DeltaServiceOrderLockRedisDAO deltaServiceOrderLockRedisDAO;
    @Resource
    private DeltaClaimOrderCoreService deltaClaimOrderCoreService;
    @Resource
    private DeltaWorkerOrderExecutionCoreService deltaWorkerOrderExecutionCoreService;
    @Resource
    private DeltaOrderProgressService deltaOrderProgressService;
    @Resource
    private DeltaOrderEvidenceService deltaOrderEvidenceService;
    @Resource
    private DeltaOrderAcceptanceService deltaOrderAcceptanceService;
    @Resource
    private DeltaOrderReworkService deltaOrderReworkService;
    @Resource
    private DeltaServiceOrderPhase6CoreService deltaServiceOrderPhase6CoreService;
    @Resource
    private DeltaServiceOrderPhase8CoreService deltaServiceOrderPhase8CoreService;
    @Resource
    private DeltaServiceOrderPhase9CoreService deltaServiceOrderPhase9CoreService;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;
    @Resource
    private MemberUserApi memberUserApi;

    // ====== Phase 3 已有 ======

    @Override
    public DeltaServiceOrderDO getServiceOrder(Long id) {
        return deltaServiceOrderMapper.selectById(id);
    }

    @Override
    public DeltaServiceOrderDO getServiceOrderByNo(String serviceOrderNo) {
        return deltaServiceOrderMapper.selectByServiceOrderNo(serviceOrderNo);
    }

    @Override
    public DeltaServiceOrderDO getServiceOrderByTradeOrderItemId(Long tradeOrderItemId) {
        return deltaServiceOrderMapper.selectByTradeOrderItemId(tradeOrderItemId);
    }

    @Override
    public List<DeltaServiceOrderDO> getServiceOrdersByTradeOrderItemIds(Collection<Long> tradeOrderItemIds) {
        return deltaServiceOrderMapper.selectListByTradeOrderItemIds(tradeOrderItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeltaServiceOrderDO> batchCreateServiceOrders(List<DeltaServiceOrderDO> orders) {
        List<DeltaServiceOrderDO> created = new ArrayList<>();
        for (DeltaServiceOrderDO order : orders) {
            try {
                order.setServiceOrderNo(deltaNoRedisDAO.generateServiceOrderNo());
                deltaServiceOrderMapper.insert(order);
                created.add(order);
            } catch (DuplicateKeyException e) {
                log.info("服务履约订单已存在(tradeOrderItemId={})，幂等跳过", order.getTradeOrderItemId());
                DeltaServiceOrderDO exist = deltaServiceOrderMapper.selectByTradeOrderItemId(order.getTradeOrderItemId());
                if (exist != null) {
                    created.add(exist);
                }
            } catch (Exception e) {
                log.error("创建服务履约订单失败(tradeOrderItemId={})", order.getTradeOrderItemId(), e);
                throw e;
            }
        }
        return created;
    }

    @Override
    public PageResult<DeltaServiceOrderDO> getServiceOrderPage(Long userId, AppDeltaServiceOrderPageReqVO pageReqVO) {
        pageReqVO.setUserId(userId);
        return deltaServiceOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public DeltaServiceOrderDO getServiceOrderForUser(Long id, Long userId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), userId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }
        return order;
    }

    @Override
    public PageResult<DeltaServiceOrderDO> getServiceOrderPage(DeltaServiceOrderPageReqVO pageReqVO) {
        return deltaServiceOrderMapper.selectPage(pageReqVO);
    }

    // ====== Phase 4 后台服务单操作 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmServiceOrder(Long id, String remark, Long adminUserId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        Integer oldStatus = order.getStatus();
        if (!ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus().equals(oldStatus)) {
            throw exception(SERVICE_ORDER_STATUS_INVALID);
        }

        // CAS 状态更新
        int rows = deltaServiceOrderMapper.updateStatusCas(
                id,
                ServiceOrderStatusEnum.POOL_PENDING.getStatus(),
                oldStatus,
                wrapper -> {}
        );
        if (rows != 1) {
            throw exception(SERVICE_ORDER_STATUS_INVALID);
        }

        // 写操作日志
        DeltaOrderLogDO logEntry = new DeltaOrderLogDO();
        logEntry.setServiceOrderId(id);
        logEntry.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        logEntry.setOperatorId(adminUserId);
        logEntry.setOperation("确认服务单");
        logEntry.setBeforeStatus(oldStatus);
        logEntry.setAfterStatus(ServiceOrderStatusEnum.POOL_PENDING.getStatus());
        logEntry.setContent(remark);
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("服务单确认成功 id={}, oldStatus={}, newStatus={}", id, oldStatus, ServiceOrderStatusEnum.POOL_PENDING.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispatchOrder(Long serviceOrderId, Long workerId, String remark, Long adminUserId) {
        // 1. 校验服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.POOL_PENDING.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_INVALID);
        }
        // 2. 校验没有有效分配
        DeltaOrderAssignmentDO existAssignment = deltaOrderAssignmentService.getActiveAssignmentByServiceOrderId(serviceOrderId);
        if (existAssignment != null) {
            throw exception(ASSIGNMENT_ALREADY_EXISTS);
        }
        // 3. 校验打手
        DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
        validateWorkerForDispatch(worker);
        validateWorkerSkill(worker, order.getDeviceType(), order.getServiceType());
        // 4. 校验打手没有有效订单
        List<DeltaOrderAssignmentDO> activeAssignments = deltaOrderAssignmentService.getActiveAssignmentsByWorkerId(workerId);
        if (CollUtil.isNotEmpty(activeAssignments)) {
            throw exception(ASSIGNMENT_WORKER_HAS_ACTIVE_ORDER);
        }

        // 5. CAS 更新服务单状态
        Integer oldStatus = order.getStatus();
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                oldStatus,
                wrapper -> wrapper.set(DeltaServiceOrderDO::getAssignedWorkerId, workerId)
        );
        if (rows != 1) {
            throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
        }

        // 6. 创建分配记录
        DeltaOrderAssignmentDO assignment = new DeltaOrderAssignmentDO();
        assignment.setServiceOrderId(serviceOrderId);
        assignment.setWorkerId(workerId);
        assignment.setAssignmentType(AssignmentTypeEnum.ADMIN_ASSIGN.getType());
        assignment.setAssignmentStatus(AssignmentStatusEnum.ACCEPTED.getStatus());
        assignment.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        assignment.setOperatorId(adminUserId);
        assignment.setReason(remark);
        deltaOrderAssignmentService.createAssignment(assignment);

        // 7. CAS 更新打手状态 ONLINE → BUSY
        int wRows = deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                workerId,
                WorkerWorkStatusEnum.BUSY.getStatus(),
                WorkerWorkStatusEnum.ONLINE.getStatus()
        );
        log.info("派单时更新打手状态 workerId={}, affected={}", workerId, wRows);

        // 8. 写操作日志
        DeltaOrderLogDO logEntry = new DeltaOrderLogDO();
        logEntry.setServiceOrderId(serviceOrderId);
        logEntry.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        logEntry.setOperatorId(adminUserId);
        logEntry.setOperation("客服派单");
        logEntry.setBeforeStatus(oldStatus);
        logEntry.setAfterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus());
        logEntry.setContent("指派打手ID=" + workerId + (remark != null ? ", 备注: " + remark : ""));
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("派单成功 serviceOrderId={}, workerId={}", serviceOrderId, workerId);

        // Phase 10: 发布派单成功事件
        try {
            Long workerUserId = worker.getUserId();
            deltaEventPublisher.publishToWorker(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_ORDER_DISPATCHED.getType())
                    .tenantId(order.getTenantId())
                    .aggregateType("SERVICE_ORDER")
                    .aggregateId(serviceOrderId)
                    .bizKey("SERVICE_ORDER_DISPATCHED:" + serviceOrderId + ":" + assignment.getId() + ":" + workerUserId)
                    .recipientId(workerUserId)
                    .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.ORDER_DISPATCHED.getCode())
                    .templateParams(java.util.Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                    .payload(cn.iocoder.yudao.module.delta.service.event.DeltaEventPayload.builder()
                            .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_ORDER_DISPATCHED.getType())
                            .tenantId(order.getTenantId())
                            .aggregateId(serviceOrderId)
                            .serviceOrderId(serviceOrderId)
                            .serviceOrderNo(order.getServiceOrderNo())
                            .buyerUserId(order.getBuyerUserId())
                            .workerId(workerId)
                            .workerUserId(workerUserId)
                            .operatorType(cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum.ADMIN.getType())
                            .operatorId(adminUserId)
                            .beforeStatus(oldStatus)
                            .afterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus())
                            .occurredAt(java.time.LocalDateTime.now())
                            .build())
                    .build());
        } catch (Exception e) {
            log.error("派单事件写入Outbox失败 serviceOrderId={}", serviceOrderId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignOrder(Long serviceOrderId, Long newWorkerId, String reason, Long adminUserId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(ASSIGNMENT_REASON_REQUIRED);
        }
        // 1. 校验服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus().equals(order.getStatus())) {
            throw exception(ASSIGNMENT_ORDER_ALREADY_STARTED);
        }
        Long oldWorkerId = order.getAssignedWorkerId();
        if (oldWorkerId == null) {
            throw exception(ASSIGNMENT_WORKER_NOT_AVAILABLE);
        }

        // 2. 校验新打手
        DeltaWorkerDO newWorker = deltaWorkerService.getWorker(newWorkerId);
        validateWorkerForDispatch(newWorker);
        validateWorkerSkill(newWorker, order.getDeviceType(), order.getServiceType());
        List<DeltaOrderAssignmentDO> newActiveAssignments = deltaOrderAssignmentService.getActiveAssignmentsByWorkerId(newWorkerId);
        if (CollUtil.isNotEmpty(newActiveAssignments)) {
            throw exception(ASSIGNMENT_WORKER_HAS_ACTIVE_ORDER);
        }

        // 3. 取旧分配记录，取消旧分配
        DeltaOrderAssignmentDO oldAssignment = deltaOrderAssignmentService.getActiveAssignmentByServiceOrderId(serviceOrderId);
        if (oldAssignment != null) {
            deltaOrderAssignmentService.cancelAssignment(oldAssignment.getId(), "改派: " + reason);
        }

        // 4. CAS 更新服务单打手
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                wrapper -> wrapper.set(DeltaServiceOrderDO::getAssignedWorkerId, newWorkerId)
        );
        if (rows != 1) {
            throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
        }

        // 5. 创建新的分配记录
        DeltaOrderAssignmentDO newAssignment = new DeltaOrderAssignmentDO();
        newAssignment.setServiceOrderId(serviceOrderId);
        newAssignment.setWorkerId(newWorkerId);
        newAssignment.setAssignmentType(AssignmentTypeEnum.REASSIGN.getType());
        newAssignment.setAssignmentStatus(AssignmentStatusEnum.ACCEPTED.getStatus());
        newAssignment.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        newAssignment.setOperatorId(adminUserId);
        newAssignment.setReason(reason);
        deltaOrderAssignmentService.createAssignment(newAssignment);

        // 6. 新打手设置为 BUSY
        deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                newWorkerId,
                WorkerWorkStatusEnum.BUSY.getStatus(),
                WorkerWorkStatusEnum.ONLINE.getStatus()
        );

        // 7. 恢复旧打手状态
        restoreWorkerStatusIfNeeded(oldWorkerId);

        // 8. 写操作日志
        DeltaOrderLogDO logEntry = new DeltaOrderLogDO();
        logEntry.setServiceOrderId(serviceOrderId);
        logEntry.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        logEntry.setOperatorId(adminUserId);
        logEntry.setOperation("改派打手");
        logEntry.setBeforeStatus(order.getStatus());
        logEntry.setAfterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus());
        logEntry.setContent("从打手ID=" + oldWorkerId + " 改派到打手ID=" + newWorkerId + ", 原因: " + reason);
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("改派成功 serviceOrderId={}, oldWorkerId={}, newWorkerId={}", serviceOrderId, oldWorkerId, newWorkerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnOrderToPool(Long serviceOrderId, String reason, Long adminUserId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(ASSIGNMENT_REASON_REQUIRED);
        }
        // 1. 校验服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus().equals(order.getStatus())) {
            throw exception(ASSIGNMENT_ORDER_ALREADY_STARTED);
        }
        Long oldWorkerId = order.getAssignedWorkerId();
        Integer oldStatus = order.getStatus();

        // 2. 取消旧分配记录
        DeltaOrderAssignmentDO oldAssignment = deltaOrderAssignmentService.getActiveAssignmentByServiceOrderId(serviceOrderId);
        if (oldAssignment != null) {
            deltaOrderAssignmentService.cancelAssignment(oldAssignment.getId(), "退回订单池: " + reason);
        }

        // 3. CAS 更新服务单: 清空 assignedWorkerId, 重置状态为 POOL_PENDING
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.POOL_PENDING.getStatus(),
                oldStatus,
                wrapper -> wrapper.set(DeltaServiceOrderDO::getAssignedWorkerId, null)
        );
        if (rows != 1) {
            throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
        }

        // 4. 恢复旧打手状态
        restoreWorkerStatusIfNeeded(oldWorkerId);

        // 5. 写操作日志
        DeltaOrderLogDO logEntry = new DeltaOrderLogDO();
        logEntry.setServiceOrderId(serviceOrderId);
        logEntry.setOperatorType(OperatorTypeEnum.ADMIN.getType());
        logEntry.setOperatorId(adminUserId);
        logEntry.setOperation("退回订单池");
        logEntry.setBeforeStatus(oldStatus);
        logEntry.setAfterStatus(ServiceOrderStatusEnum.POOL_PENDING.getStatus());
        logEntry.setContent("从打手ID=" + (oldWorkerId != null ? oldWorkerId : "无") + " 退回订单池, 原因: " + reason);
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("退回订单池成功 serviceOrderId={}, oldWorkerId={}", serviceOrderId, oldWorkerId);
    }

    // ====== Phase 4 App 打手操作 ======

    @Override
    public void claimOrder(Long loginUserId, Long serviceOrderId) {
        // 1. 先查打手身份（锁外查询，避免重查询）
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        validateWorkerForDispatch(worker);

        // 2. 使用 Redisson 锁，锁内通过独立 Bean 调用事务方法
        //    deltaClaimOrderCoreService 是独立 Bean，@Transactional 会通过 Spring AOP 代理生效
        Long tenantId = worker.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaClaimOrderCoreService.doClaimOrder(worker, serviceOrderId);
            return null;
        });
    }

    // ====== 订单池 & 打手订单查询 ======

    @Override
    public PageResult<DeltaServiceOrderDO> getPoolPage(Long workerId, Integer deviceType, Integer serviceType, PageParam pageParam) {
        return deltaServiceOrderMapper.selectPoolPage(pageParam,
                ServiceOrderStatusEnum.POOL_PENDING.getStatus(), deviceType, serviceType);
    }

    @Override
    public DeltaServiceOrderDO getPoolDetail(Long id, Long workerId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.POOL_PENDING.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_INVALID);
        }
        if (order.getAssignedWorkerId() != null) {
            throw exception(SERVICE_ORDER_ALREADY_CLAIMED);
        }
        // 校验技能匹配
        if (!deltaWorkerSkillMapper.hasMatchingSkill(workerId, order.getDeviceType(), order.getServiceType())) {
            throw exception(ASSIGNMENT_WORKER_SKILL_NOT_MATCH);
        }
        return order;
    }

    @Override
    public PageResult<DeltaServiceOrderDO> getWorkerOrderPage(Long workerId, Integer status, PageParam pageParam) {
        DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        return deltaServiceOrderMapper.selectWorkerPage(pageParam, workerId, status);
    }

    @Override
    public DeltaServiceOrderDO getWorkerOrderDetail(Long id, Long workerId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(id);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getAssignedWorkerId(), workerId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        return order;
    }

    // ====== 内部辅助校验 ======

    private void validateWorkerForDispatch(DeltaWorkerDO worker) {
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
        // 校验至少有启用技能
        List<DeltaWorkerSkillDO> skills = deltaWorkerSkillMapper.selectEnabledListByWorkerId(worker.getId());
        if (CollUtil.isEmpty(skills)) {
            throw exception(ASSIGNMENT_WORKER_SKILL_NOT_MATCH);
        }
    }

    private void validateWorkerSkill(DeltaWorkerDO worker, Integer deviceType, Integer serviceType) {
        if (!deltaWorkerSkillMapper.hasMatchingSkill(worker.getId(), deviceType, serviceType)) {
            throw exception(ASSIGNMENT_WORKER_SKILL_NOT_MATCH);
        }
    }

    private void restoreWorkerStatusIfNeeded(Long workerId) {
        if (workerId == null) {
            return;
        }
        DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
        if (worker == null || !WorkerWorkStatusEnum.BUSY.getStatus().equals(worker.getWorkStatus())) {
            return;
        }
        // 检查是否还有其他有效分配
        List<DeltaOrderAssignmentDO> activeAssignments = deltaOrderAssignmentService.getActiveAssignmentsByWorkerId(workerId);
        if (CollUtil.isEmpty(activeAssignments)) {
            // 没有其他有效订单，恢复为 ONLINE
            deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                    workerId,
                    WorkerWorkStatusEnum.ONLINE.getStatus(),
                    WorkerWorkStatusEnum.BUSY.getStatus()
            );
            log.info("恢复打手状态 workerId={}, BUSY→ONLINE", workerId);
        }
    }

    // ====== Phase 5 打手服务执行 ======

    @Override
    public void startService(Long loginUserId, Long serviceOrderId) {
        // 1. 获取打手身份
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        validateWorkerForServiceExecution(worker);

        // 2. 使用 Redisson 锁 + 独立事务 Bean
        Long tenantId = worker.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaWorkerOrderExecutionCoreService.doStartService(worker, serviceOrderId);
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderProgressDO createProgress(Long loginUserId, AppDeltaWorkerOrderProgressCreateReqVO reqVO) {
        // 1. 获取打手身份
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }

        // 2. 校验服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(reqVO.getServiceOrderId());
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!worker.getId().equals(order.getAssignedWorkerId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_PROGRESS);
        }

        // 3. 校验进度类型（不允许伪造系统关键类型）
        if (ProgressTypeEnum.START_SERVICE.getType().equals(reqVO.getProgressType())
                || ProgressTypeEnum.SUBMIT_COMPLETION.getType().equals(reqVO.getProgressType())) {
            throw exception(PROGRESS_TYPE_FORBIDDEN);
        }

        // 4. 校验内容
        if (reqVO.getContent() == null || reqVO.getContent().trim().isEmpty()) {
            throw exception(PROGRESS_CONTENT_EMPTY);
        }

        // 5. 校验百分比
        Integer percent = reqVO.getProgressPercent();
        if (percent != null && (percent < 0 || percent > 100)) {
            throw exception(PROGRESS_PERCENT_INVALID);
        }

        // 6. 创建进度
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(reqVO.getServiceOrderId())
                .workerId(worker.getId())
                .progressType(reqVO.getProgressType())
                .progressPercent(percent)
                .content(reqVO.getContent())
                .build();
        deltaOrderProgressService.createProgress(progress);
        log.info("提交服务进度 serviceOrderId={}, workerId={}, progressType={}", reqVO.getServiceOrderId(), worker.getId(), reqVO.getProgressType());
        return progress;
    }

    @Override
    public List<DeltaOrderProgressDO> getWorkerProgressList(Long loginUserId, Long serviceOrderId) {
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!worker.getId().equals(order.getAssignedWorkerId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        return deltaOrderProgressService.getProgressListByServiceOrderId(serviceOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderEvidenceDO createEvidence(Long loginUserId, AppDeltaWorkerOrderEvidenceCreateReqVO reqVO) {
        // 1. 获取打手身份
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }

        // 2. 校验服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(reqVO.getServiceOrderId());
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!worker.getId().equals(order.getAssignedWorkerId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_EVIDENCE);
        }

        // 3. 校验凭证类型合法
        boolean validType = false;
        for (EvidenceTypeEnum e : EvidenceTypeEnum.values()) {
            if (e.getType().equals(reqVO.getEvidenceType())) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_EVIDENCE);
        }

        // 4. 校验文件URL（HTTPS或项目支持的文件URL）
        if (reqVO.getFileUrl() == null || reqVO.getFileUrl().trim().isEmpty()) {
            throw exception(EVIDENCE_URL_EMPTY);
        }

        // 5. 校验数量上限
        long count = deltaOrderEvidenceService.countEvidenceByServiceOrderId(reqVO.getServiceOrderId());
        if (count >= DeltaWorkerOrderExecutionCoreService.MAX_EVIDENCE_PER_ORDER) {
            throw exception(EVIDENCE_COUNT_EXCEED);
        }

        // 6. 构建凭证（映射到现有字段结构）
        DeltaOrderEvidenceDO evidence = DeltaOrderEvidenceDO.builder()
                .serviceOrderId(reqVO.getServiceOrderId())
                .workerId(worker.getId())
                .evidenceType(reqVO.getEvidenceType())
                .content(reqVO.getDescription())
                .imageUrls(Collections.singletonList(reqVO.getFileUrl()))
                .build();
        deltaOrderEvidenceService.createEvidence(evidence);

        log.info("登记服务凭证 serviceOrderId={}, workerId={}, evidenceType={}", reqVO.getServiceOrderId(), worker.getId(), reqVO.getEvidenceType());
        return evidence;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvidence(Long loginUserId, Long evidenceId) {
        // 1. 获取打手身份
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }

        // 2. 查凭证
        DeltaOrderEvidenceDO evidence = deltaOrderEvidenceService.getEvidence(evidenceId);
        if (evidence == null) {
            throw exception(EVIDENCE_NOT_EXISTS_P5);
        }
        if (!worker.getId().equals(evidence.getWorkerId())) {
            throw exception(EVIDENCE_NOT_BELONG_TO_WORKER);
        }

        // 3. 校验服务单状态（已提交完成不能删除）
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(evidence.getServiceOrderId());
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(EVIDENCE_CANNOT_OPERATE_AFTER_COMPLETE);
        }

        // 4. 逻辑删除
        deltaOrderEvidenceService.deleteEvidence(evidenceId);

        // 5. 写删除日志
        DeltaOrderLogDO logEntry = DeltaOrderLogDO.builder()
                .serviceOrderId(order.getId())
                .operatorType(OperatorTypeEnum.WORKER.getType())
                .operatorId(worker.getId())
                .operation("删除凭证")
                .beforeStatus(order.getStatus())
                .afterStatus(order.getStatus())
                .content("打手ID=" + worker.getId() + " 删除凭证ID=" + evidenceId)
                .build();
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("删除服务凭证 evidenceId={}, workerId={}", evidenceId, worker.getId());
    }

    @Override
    public List<DeltaOrderEvidenceDO> getWorkerEvidenceList(Long loginUserId, Long serviceOrderId) {
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!worker.getId().equals(order.getAssignedWorkerId())) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_WORKER);
        }
        return deltaOrderEvidenceService.getEvidenceListByServiceOrderId(serviceOrderId);
    }

    @Override
    public void submitCompletion(Long loginUserId, Long serviceOrderId, String summary) {
        // 1. 获取打手身份
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(loginUserId);
        if (worker == null) {
            throw exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        validateWorkerForServiceExecution(worker);

        // 2. 校验总结
        if (summary == null || summary.trim().isEmpty()) {
            throw exception(COMPLETION_SUMMARY_EMPTY);
        }

        // 3. 使用 Redisson 锁 + 独立事务 Bean
        Long tenantId = worker.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaWorkerOrderExecutionCoreService.doSubmitCompletion(worker, serviceOrderId, summary);
            return null;
        });
    }

    // ====== Phase 5 老板查询 ======

    @Override
    public List<DeltaOrderProgressDO> getBuyerProgressList(Long loginUserId, Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), loginUserId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }
        return deltaOrderProgressService.getProgressListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderEvidenceDO> getBuyerEvidenceList(Long loginUserId, Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), loginUserId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }
        return deltaOrderEvidenceService.getEvidenceListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<AppDeltaServiceOrderTimelineRespVO> getBuyerTimeline(Long loginUserId, Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), loginUserId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }
        // 买家时间线不暴露管理员、内部备注等信息
        return buildTimeline(serviceOrderId, false);
    }

    // ====== Phase 5 后台查询 ======

    @Override
    public List<DeltaOrderProgressDO> getProgressListByServiceOrderId(Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        return deltaOrderProgressService.getProgressListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderId(Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        return deltaOrderEvidenceService.getEvidenceListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<AppDeltaServiceOrderTimelineRespVO> getTimeline(Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        // 后台可以看到管理员操作类型
        return buildTimeline(serviceOrderId, true);
    }

    // ====== Phase 5 内部辅助 ======

    private void validateWorkerForServiceExecution(DeltaWorkerDO worker) {
        if (worker == null) {
            throw exception(ASSIGNMENT_WORKER_NOT_AVAILABLE);
        }
        if (!WorkerAuditStatusEnum.isApproved(worker.getAuditStatus())) {
            throw exception(WORKER_NOT_APPROVED);
        }
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            throw exception(WORKER_DISABLED);
        }
    }

    /**
     * 构建履约时间线（批量聚合，无 N+1）
     *
     * @param serviceOrderId 服务单ID
     * @param isAdmin        是否后台（暴露管理员操作类型）
     */
    private List<AppDeltaServiceOrderTimelineRespVO> buildTimeline(Long serviceOrderId, boolean isAdmin) {
        List<AppDeltaServiceOrderTimelineRespVO> timeline = new ArrayList<>();

        // 1. 批量查询日志
        List<DeltaOrderLogDO> logs = deltaOrderLogService.getLogsByServiceOrderId(serviceOrderId);
        // 2. 批量查询进度
        List<DeltaOrderProgressDO> progresses = deltaOrderProgressService.getProgressListByServiceOrderId(serviceOrderId);
        // 3. 批量查询凭证
        List<DeltaOrderEvidenceDO> evidences = deltaOrderEvidenceService.getEvidenceListByServiceOrderId(serviceOrderId);

        // 收集需要查询名称的打手ID集合
        Set<Long> workerIdSet = progresses.stream()
                .map(DeltaOrderProgressDO::getWorkerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        evidences.stream()
                .map(DeltaOrderEvidenceDO::getWorkerId)
                .filter(Objects::nonNull)
                .forEach(workerIdSet::add);
        logs.stream()
                .filter(log -> OperatorTypeEnum.WORKER.getType().equals(log.getOperatorType()))
                .map(DeltaOrderLogDO::getOperatorId)
                .filter(Objects::nonNull)
                .forEach(workerIdSet::add);

        // 批量查询打手displayName
        Map<Long, String> workerNameMap = new LinkedHashMap<>();
        for (Long wid : workerIdSet) {
            DeltaWorkerDO w = deltaWorkerService.getWorker(wid);
            if (w != null) {
                workerNameMap.put(wid, w.getDisplayName());
            }
        }

        // 收集管理员ID
        Set<Long> adminIdSet = logs.stream()
                .filter(log -> OperatorTypeEnum.ADMIN.getType().equals(log.getOperatorType()))
                .map(DeltaOrderLogDO::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 日志节点
        for (DeltaOrderLogDO log : logs) {
            AppDeltaServiceOrderTimelineRespVO node = new AppDeltaServiceOrderTimelineRespVO();
            node.setNodeType("LOG");
            node.setTitle(log.getOperation());
            node.setContent(log.getContent());
            node.setOperatorType(getOperatorTypeName(log.getOperatorType(), isAdmin));
            node.setOperatorName(getOperatorDisplayName(log.getOperatorType(), log.getOperatorId(),
                    workerNameMap, adminIdSet));
            node.setEventTime(log.getCreateTime());
            timeline.add(node);
        }

        // 进度节点
        for (DeltaOrderProgressDO prog : progresses) {
            AppDeltaServiceOrderTimelineRespVO node = new AppDeltaServiceOrderTimelineRespVO();
            node.setNodeType("PROGRESS");
            String title = "进度更新";
            for (ProgressTypeEnum e : ProgressTypeEnum.values()) {
                if (e.getType().equals(prog.getProgressType())) {
                    title = e.getName();
                    break;
                }
            }
            node.setTitle(title);
            node.setContent(prog.getContent());
            node.setOperatorType(isAdmin ? "WORKER" : null);
            node.setOperatorName(workerNameMap.get(prog.getWorkerId()));
            node.setEventTime(prog.getCreateTime());
            timeline.add(node);
        }

        // 凭证节点
        for (DeltaOrderEvidenceDO ev : evidences) {
            AppDeltaServiceOrderTimelineRespVO node = new AppDeltaServiceOrderTimelineRespVO();
            node.setNodeType("EVIDENCE");
            String evTitle = "登记凭证";
            for (EvidenceTypeEnum e : EvidenceTypeEnum.values()) {
                if (e.getType().equals(ev.getEvidenceType())) {
                    evTitle = "登记凭证-" + e.getName();
                    break;
                }
            }
            node.setTitle(evTitle);
            node.setContent(ev.getContent());
            node.setOperatorType(isAdmin ? "WORKER" : null);
            node.setOperatorName(workerNameMap.get(ev.getWorkerId()));
            node.setEventTime(ev.getCreateTime());
            timeline.add(node);
        }

        // 按时间排序
        timeline.sort((a, b) -> {
            if (a.getEventTime() == null && b.getEventTime() == null) return 0;
            if (a.getEventTime() == null) return -1;
            if (b.getEventTime() == null) return 1;
            return a.getEventTime().compareTo(b.getEventTime());
        });

        return timeline;
    }

    private String getOperatorTypeName(Integer operatorType, boolean isAdmin) {
        if (operatorType == null) return null;
        for (OperatorTypeEnum e : OperatorTypeEnum.values()) {
            if (e.getType().equals(operatorType)) {
                if (!isAdmin && OperatorTypeEnum.ADMIN.getType().equals(operatorType)) {
                    return "客服";
                }
                return e.getName();
            }
        }
        return null;
    }

    private String getOperatorDisplayName(Integer operatorType, Long operatorId,
                                           Map<Long, String> workerNameMap, Set<Long> adminIdSet) {
        if (operatorType == null || operatorId == null) return null;
        if (OperatorTypeEnum.WORKER.getType().equals(operatorType)) {
            return workerNameMap.getOrDefault(operatorId, "打手" + operatorId);
        }
        if (OperatorTypeEnum.ADMIN.getType().equals(operatorType)) {
            return "管理员" + operatorId;
        }
        return null;
    }

    // ====== Phase 6 验收与返工 ======

    @Override
    public void acceptByBuyer(Long loginUserId, Long serviceOrderId, String remark) {
        // 1. 校验订单归属
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), loginUserId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }

        // 2. 使用 Redisson 锁 + 独立事务 Bean
        Long tenantId = order.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaServiceOrderPhase6CoreService.doAcceptByBuyer(loginUserId, serviceOrderId, remark);
            return null;
        });
    }

    @Override
    public void requestReworkByBuyer(Long loginUserId, Long serviceOrderId, String reason) {
        // 1. 校验原因必填
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(REWORK_REASON_EMPTY);
        }

        // 2. 校验订单归属
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getBuyerUserId(), loginUserId)) {
            throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);
        }

        // 3. 使用 Redisson 锁 + 独立事务 Bean
        Long tenantId = order.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaServiceOrderPhase6CoreService.doRequestReworkByBuyer(loginUserId, serviceOrderId, reason);
            return null;
        });
    }

    @Override
    public void acceptByAdmin(Long adminUserId, Long serviceOrderId, String remark) {
        // 直接使用 Redisson 锁 + 独立事务 Bean
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        Long tenantId = order.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaServiceOrderPhase6CoreService.doAcceptByAdmin(adminUserId, serviceOrderId, remark);
            return null;
        });
    }

    @Override
    public void requestReworkByAdmin(Long adminUserId, Long serviceOrderId, String reason) {
        // 1. 校验原因必填
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(REWORK_REASON_EMPTY);
        }

        // 2. 直接使用 Redisson 锁 + 独立事务 Bean
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        Long tenantId = order.getTenantId();
        deltaServiceOrderLockRedisDAO.lockAndRun(tenantId, serviceOrderId, () -> {
            deltaServiceOrderPhase6CoreService.doRequestReworkByAdmin(adminUserId, serviceOrderId, reason);
            return null;
        });
    }

    @Override
    public List<DeltaOrderAcceptanceRespVO> getAcceptanceList(Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        List<DeltaOrderAcceptanceDO> list = deltaOrderAcceptanceService.getAcceptanceListByServiceOrderId(serviceOrderId);
        List<DeltaOrderAcceptanceRespVO> voList = DeltaOrderAcceptanceConvert.INSTANCE.convertList(list);
        voList.forEach(DeltaOrderAcceptanceConvert.INSTANCE::fillNames);
        return voList;
    }

    @Override
    public List<DeltaOrderReworkRespVO> getReworkList(Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        List<DeltaOrderReworkDO> list = deltaOrderReworkService.getReworkListByServiceOrderId(serviceOrderId);
        List<DeltaOrderReworkRespVO> voList = DeltaOrderReworkConvert.INSTANCE.convertList(list);
        voList.forEach(DeltaOrderReworkConvert.INSTANCE::fillNames);
        return voList;
    }

    // ====== Phase 8 取消与售后 ======

    @Override
    public Long applyCancelByBuyer(Long buyerUserId, Long serviceOrderId, String reason, String remark) {
        return deltaServiceOrderPhase8CoreService.applyCancelByBuyer(buyerUserId, serviceOrderId, reason, remark).getId();
    }

    @Override
    public PageResult<AppDeltaCancelRespVO>
            getBuyerCancelPage(Long buyerUserId, PageParam pageParam) {
        PageResult<DeltaOrderCancelDO> page
                = deltaOrderCancelMapper.selectPageByBuyer(buyerUserId, null, pageParam);
        List<AppDeltaCancelRespVO> vos = new ArrayList<>();
        for (DeltaOrderCancelDO c : page.getList()) {
            AppDeltaCancelRespVO vo
                    = new AppDeltaCancelRespVO();
            vo.setId(c.getId());
            vo.setCancelNo(c.getCancelNo());
            vo.setServiceOrderId(c.getServiceOrderId());
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(c.getServiceOrderId());
            if (order != null) vo.setServiceOrderNo(order.getServiceOrderNo());
            vo.setApplyReason(c.getApplyReason());
            vo.setStatus(c.getApplyStatus());
            vo.setStatusName(c.getApplyStatus() == 0 ? "待审核" : (c.getApplyStatus() == 1 ? "已通过" : "已驳回"));
            vo.setRefundAmount(c.getRefundAmount());
            vo.setReviewRemark(c.getReviewRemark());
            vo.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().toString() : null);
            vo.setReviewTime(c.getReviewTime() != null ? c.getReviewTime().toString() : null);
            vos.add(vo);
        }
        return new PageResult<>(vos, page.getTotal());
    }

    @Override
    public AppDeltaCancelRespVO
            getBuyerCancelDetail(Long buyerUserId, Long cancelId) {
        DeltaOrderCancelDO c
                = deltaOrderCancelMapper.selectById(cancelId);
        if (c == null) throw exception(CANCEL_NOT_EXISTS);
        if (!c.getBuyerUserId().equals(buyerUserId)) throw exception(CANCEL_NOT_BELONG_TO_USER);
        AppDeltaCancelRespVO vo
                = new AppDeltaCancelRespVO();
        vo.setId(c.getId());
        vo.setCancelNo(c.getCancelNo());
        vo.setServiceOrderId(c.getServiceOrderId());
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(c.getServiceOrderId());
        if (order != null) vo.setServiceOrderNo(order.getServiceOrderNo());
        vo.setApplyReason(c.getApplyReason());
        vo.setStatus(c.getApplyStatus());
        vo.setStatusName(c.getApplyStatus() == 0 ? "待审核" : (c.getApplyStatus() == 1 ? "已通过" : "已驳回"));
        vo.setRefundAmount(c.getRefundAmount());
        vo.setReviewRemark(c.getReviewRemark());
        vo.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().toString() : null);
        vo.setReviewTime(c.getReviewTime() != null ? c.getReviewTime().toString() : null);
        return vo;
    }

    @Override
    public Long applyAfterSaleByBuyer(Long buyerUserId, Long serviceOrderId, Integer afterSaleType,
                                       Integer reasonType, String reason, String description,
                                       Integer requestedRefundAmount, String evidenceUrls) {
        return deltaServiceOrderPhase8CoreService.applyAfterSaleByBuyer(buyerUserId, serviceOrderId,
                afterSaleType, reasonType, reason, description, requestedRefundAmount, evidenceUrls).getId();
    }

    @Override
    public PageResult<AppDeltaAfterSaleRespVO>
            getBuyerAfterSalePage(Long buyerUserId, Integer status, PageParam pageParam) {
        PageResult<DeltaAfterSaleDO> page
                = deltaAfterSaleMapper.selectPageByBuyer(buyerUserId, status, pageParam);
        List<AppDeltaAfterSaleRespVO> vos = new ArrayList<>();
        for (DeltaAfterSaleDO a : page.getList()) {
            AppDeltaAfterSaleRespVO vo
                    = new AppDeltaAfterSaleRespVO();
            vo.setId(a.getId());
            vo.setAfterSaleNo(a.getAfterSaleNo());
            vo.setServiceOrderId(a.getServiceOrderId());
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(a.getServiceOrderId());
            if (order != null) vo.setServiceOrderNo(order.getServiceOrderNo());
            vo.setAfterSaleType(a.getAfterSaleType());
            vo.setReason(a.getReason());
            vo.setDescription(a.getDescription());
            vo.setRequestedRefundAmount(a.getRequestedRefundAmount());
            vo.setApprovedRefundAmount(a.getApprovedRefundAmount());
            vo.setStatus(a.getStatus());
            vo.setStatusName(getAfterSaleStatusName(a.getStatus()));
            DeltaAfterSaleArbitrationDO arb
                    = deltaAfterSaleArbitrationMapper.selectByAfterSaleId(a.getId());
            if (arb != null) {
                vo.setArbitrationResult(arb.getDecisionType() == 1 ? "全额退款"
                        : (arb.getDecisionType() == 2 ? "部分退款"
                        : (arb.getDecisionType() == 3 ? "继续服务" : "不退款")));
            }
            vo.setCreateTime(a.getCreateTime() != null ? a.getCreateTime().toString() : null);
            vo.setHandleTime(a.getHandleTime() != null ? a.getHandleTime().toString() : null);
            vos.add(vo);
        }
        return new PageResult<>(vos, page.getTotal());
    }

    @Override
    public AppDeltaAfterSaleRespVO
            getBuyerAfterSaleDetail(Long buyerUserId, Long afterSaleId) {
        DeltaAfterSaleDO a
                = deltaAfterSaleMapper.selectById(afterSaleId);
        if (a == null) throw exception(AFTER_SALE_NOT_EXISTS);
        if (!a.getBuyerUserId().equals(buyerUserId)) throw exception(AFTER_SALE_NOT_BELONG_TO_USER);
        AppDeltaAfterSaleRespVO vo
                = new AppDeltaAfterSaleRespVO();
        vo.setId(a.getId());
        vo.setAfterSaleNo(a.getAfterSaleNo());
        vo.setServiceOrderId(a.getServiceOrderId());
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(a.getServiceOrderId());
        if (order != null) vo.setServiceOrderNo(order.getServiceOrderNo());
        vo.setAfterSaleType(a.getAfterSaleType());
        vo.setReason(a.getReason());
        vo.setDescription(a.getDescription());
        vo.setRequestedRefundAmount(a.getRequestedRefundAmount());
        vo.setApprovedRefundAmount(a.getApprovedRefundAmount());
        vo.setStatus(a.getStatus());
        vo.setStatusName(getAfterSaleStatusName(a.getStatus()));
        DeltaAfterSaleArbitrationDO arb
                = deltaAfterSaleArbitrationMapper.selectByAfterSaleId(a.getId());
        if (arb != null) {
            vo.setArbitrationResult(arb.getDecisionType() == 1 ? "全额退款"
                    : (arb.getDecisionType() == 2 ? "部分退款"
                    : (arb.getDecisionType() == 3 ? "继续服务" : "不退款")));
        }
        vo.setCreateTime(a.getCreateTime() != null ? a.getCreateTime().toString() : null);
        vo.setHandleTime(a.getHandleTime() != null ? a.getHandleTime().toString() : null);
        return vo;
    }

    @Override
    public void approveCancelByAdmin(Long adminUserId, Long cancelId, Integer refundAmount,
                                      Integer responsibilityType, String remark) {
        deltaServiceOrderPhase8CoreService.approveCancelByAdmin(adminUserId, cancelId, refundAmount,
                responsibilityType, remark);
    }

    @Override
    public void rejectCancelByAdmin(Long adminUserId, Long cancelId, String reason) {
        deltaServiceOrderPhase8CoreService.rejectCancelByAdmin(adminUserId, cancelId, reason);
    }

    @Override
    public void acceptAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark) {
        deltaServiceOrderPhase8CoreService.acceptAfterSaleByAdmin(adminUserId, afterSaleId, remark);
    }

    @Override
    public void rejectAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String reason) {
        deltaServiceOrderPhase8CoreService.rejectAfterSaleByAdmin(adminUserId, afterSaleId, reason);
    }

    @Override
    public void arbitrateAfterSaleByAdmin(Long adminUserId, Long afterSaleId, Integer decisionType,
                                           Integer refundAmount, Integer responsibilityType,
                                           Integer workerDeductionAmount, Integer platformBearAmount, String remark) {
        deltaServiceOrderPhase8CoreService.arbitrateAfterSaleByAdmin(adminUserId, afterSaleId,
                decisionType, refundAmount, responsibilityType,
                workerDeductionAmount, platformBearAmount, remark);
    }

    @Override
    public void closeAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark) {
        deltaServiceOrderPhase8CoreService.closeAfterSaleByAdmin(adminUserId, afterSaleId, remark);
    }

    @Override
    public PageResult<DeltaOrderCancelDO>
            getCancelPage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaOrderCancelPageReqVO reqVO) {
        return deltaOrderCancelMapper.selectPage(reqVO);
    }

    @Override
    public DeltaOrderCancelDO
            getCancelDetail(Long cancelId) {
        return deltaOrderCancelMapper.selectById(cancelId);
    }

    @Override
    public PageResult<DeltaAfterSaleDO>
            getAfterSalePage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaAfterSalePageReqVO reqVO) {
        return deltaAfterSaleMapper.selectPage(reqVO);
    }

    @Override
    public DeltaAfterSaleDO
            getAfterSaleDetail(Long afterSaleId) {
        return deltaAfterSaleMapper.selectById(afterSaleId);
    }

    private String getAfterSaleStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待处理";
            case 1: return "已受理";
            case 2: return "已驳回";
            case 3: return "已仲裁";
            case 4: return "已关闭";
            default: return "";
        }
    }

    // Mappers for Phase 8 (used by the service impl methods)
    @Resource
    private DeltaOrderCancelMapper deltaOrderCancelMapper;
    @Resource
    private DeltaAfterSaleMapper deltaAfterSaleMapper;
    @Resource
    private DeltaAfterSaleArbitrationMapper deltaAfterSaleArbitrationMapper;
    // Mappers for Phase 9
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;

    // ====== Phase 9 退款查询 ======

    @Override
    public PageResult<AppDeltaRefundRespVO>
            getBuyerRefundPage(Long buyerUserId, PageParam pageParam) {
        QueryWrapper<DeltaRefundRecordDO>  wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DeltaRefundRecordDO::getBuyerUserId, buyerUserId)
                .orderByDesc(DeltaRefundRecordDO::getId);

        PageResult<DeltaRefundRecordDO> pageResult
                = deltaRefundRecordMapper.selectPage(pageParam,wrapper);
        List<AppDeltaRefundRespVO> voList
                = new ArrayList<>();
        for (DeltaRefundRecordDO r : pageResult.getList()) {
            voList.add(toAppRefundVO(r));
        }
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public AppDeltaRefundRespVO
            getBuyerRefundDetail(Long buyerUserId, Long refundId) {
        DeltaRefundRecordDO record
                = deltaRefundRecordMapper.selectById(refundId);
        if (record == null) throw exception(REFUND_RECORD_NOT_EXISTS);
        if (!record.getBuyerUserId().equals(buyerUserId)) throw exception(REFUND_NOT_BELONG_TO_USER);
        return toAppRefundVO(record);
    }

    private AppDeltaRefundRespVO
            toAppRefundVO(DeltaRefundRecordDO r) {
        AppDeltaRefundRespVO vo
                = new AppDeltaRefundRespVO();
        vo.setId(r.getId());
        vo.setRefundNo(r.getRefundNo());
        vo.setServiceOrderId(r.getServiceOrderId());
        vo.setRefundAmount(r.getRefundAmount());
        vo.setRefundStatus(r.getRefundStatus());
        vo.setRefundStatusName(getRefundStatusName(r.getRefundStatus()));
        vo.setRefundMethod(r.getRefundMethod());
        vo.setRefundMethodName(getRefundMethodName(r.getRefundMethod()));
        vo.setCompletedTime(r.getCompletedTime() != null ? r.getCompletedTime().toString() : null);
        vo.setProcessRemark(r.getProcessRemark());
        vo.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().toString() : null);
        return vo;
    }

    private String getRefundStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待人工退款";
            case 1: return "人工退款处理中";
            case 2: return "人工退款已完成";
            case 3: return "已取消";
            case 4: return "人工退款失败";
            default: return "";
        }
    }

    private String getRefundMethodName(Integer method) {
        if (method == null) return "";
        switch (method) {
            case 1: return "人工微信";
            case 2: return "银行卡";
            case 3: return "支付宝";
            case 4: return "其他";
            default: return "";
        }
    }

}
