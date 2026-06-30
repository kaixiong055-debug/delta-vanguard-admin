package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.*;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaServiceOrderLockRedisDAO;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.order.*;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementLogService;
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
 * Phase 8 取消、售后、仲裁、退款核心事务 Service（独立 Bean 确保 @Transactional 生效）
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaServiceOrderPhase8CoreService {

    /** 售后申请时间限制：完成后 7 天 */
    public static final int AFTER_SALE_MAX_DAYS_AFTER_COMPLETED = 7;

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaOrderCancelMapper deltaOrderCancelMapper;
    @Resource
    private DeltaAfterSaleMapper deltaAfterSaleMapper;
    @Resource
    private DeltaAfterSaleArbitrationMapper deltaAfterSaleArbitrationMapper;
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaOrderAssignmentMapper deltaOrderAssignmentMapper;
    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;
    @Resource
    private DeltaWorkerService deltaWorkerService;
    @Resource
    private DeltaOrderLogService deltaOrderLogService;
    @Resource
    private DeltaWorkerSettlementLogService deltaWorkerSettlementLogService;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;
    @Resource
    private DeltaServiceOrderLockRedisDAO deltaServiceOrderLockRedisDAO;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;

    // ======================== 买家取消申请 ========================

    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderCancelDO applyCancelByBuyer(Long buyerUserId, Long serviceOrderId, String reason, String remark) {
        // 1. 查询服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) throw exception(SERVICE_ORDER_NOT_EXISTS);
        if (!order.getBuyerUserId().equals(buyerUserId)) throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);

        // 2. 状态必须允许取消（PENDING_DISPATCH/POOL_PENDING/ACCEPTED_PENDING_START）
        if (!ServiceOrderStatusEnum.canCancel(order.getStatus())) {
            throw exception(CANCEL_ORDER_STATUS_NOT_ALLOWED);
        }

        // 3. 同一服务单不能有未处理的取消申请
        DeltaOrderCancelDO existing = deltaOrderCancelMapper.selectPendingByServiceOrderId(serviceOrderId);
        if (existing != null) throw exception(CANCEL_ALREADY_PENDING);

        // 4. 创建取消申请
        String cancelNo = deltaNoRedisDAO.generateCancelNo();
        DeltaOrderCancelDO cancel = DeltaOrderCancelDO.builder()
                .cancelNo(cancelNo)
                .serviceOrderId(serviceOrderId)
                .buyerUserId(buyerUserId)
                .workerId(order.getAssignedWorkerId())
                .applyReason(reason)
                .applyRemark(remark)
                .applyStatus(CancelStatusEnum.PENDING.getStatus())
                .originalOrderStatus(order.getStatus())
                .cancelType(1)
                .build();
        deltaOrderCancelMapper.insert(cancel);

        // 5. 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(OperatorTypeEnum.CUSTOMER.getType())
                .operatorId(buyerUserId)
                .operation("提交取消申请")
                .beforeStatus(order.getStatus())
                .afterStatus(order.getStatus())
                .content("买家ID=" + buyerUserId + " 提交取消申请，原因：" + reason
                        + (remark != null ? "，备注：" + remark : ""))
                .build());

        log.info("取消申请创建成功 cancelId={}, serviceOrderId={}", cancel.getId(), serviceOrderId);
        return cancel;
    }

    // ======================== 买家售后申请 ========================

    @Transactional(rollbackFor = Exception.class)
    public DeltaAfterSaleDO applyAfterSaleByBuyer(Long buyerUserId, Long serviceOrderId,
                                                    Integer afterSaleType, Integer reasonType,
                                                    String reason, String description,
                                                    Integer requestedRefundAmount, String evidenceUrls) {
        // 1. 查询服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) throw exception(SERVICE_ORDER_NOT_EXISTS);
        if (!order.getBuyerUserId().equals(buyerUserId)) throw exception(SERVICE_ORDER_NOT_BELONG_TO_USER);

        if (reason == null || reason.trim().isEmpty()) throw exception(AFTER_SALE_REASON_EMPTY);

        // 2. 状态必须允许售后
        if (!ServiceOrderStatusEnum.canAfterSale(order.getStatus())) {
            throw exception(AFTER_SALE_ORDER_STATUS_NOT_ALLOWED);
        }

        // 3. 完成后检查时间限制
        if (ServiceOrderStatusEnum.isCompleted(order.getStatus()) && order.getCompletedAt() != null) {
            if (LocalDateTime.now().isAfter(order.getCompletedAt().plusDays(AFTER_SALE_MAX_DAYS_AFTER_COMPLETED))) {
                throw exception(AFTER_SALE_TIME_EXCEED);
            }
        }

        // 4. 退款金额不能超过服务金额
        if (requestedRefundAmount != null) {
            if (requestedRefundAmount < 0 || requestedRefundAmount > order.getServiceAmount()) {
                throw exception(AFTER_SALE_REFUND_AMOUNT_INVALID);
            }
        }

        // 5. 同一服务单不能有未结束的售后
        DeltaAfterSaleDO existing = deltaAfterSaleMapper.selectActiveByServiceOrderId(serviceOrderId);
        if (existing != null) throw exception(AFTER_SALE_ALREADY_ACTIVE);

        // 6. CAS 更新服务单状态：进入 AFTER_SALE
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.AFTER_SALE.getStatus(),
                order.getStatus(),
                wrapper -> {}
        );
        if (rows != 1) throw exception(SERVICE_ORDER_STATUS_CHANGED);

        // 7. 创建售后案件
        String afterSaleNo = deltaNoRedisDAO.generateAfterSaleNo();
        DeltaAfterSaleDO afterSale = DeltaAfterSaleDO.builder()
                .afterSaleNo(afterSaleNo)
                .serviceOrderId(serviceOrderId)
                .buyerUserId(buyerUserId)
                .workerId(order.getAssignedWorkerId())
                .afterSaleType(afterSaleType)
                .reasonType(reasonType)
                .reason(reason)
                .description(description)
                .evidenceUrls(evidenceUrls)
                .status(AfterSaleStatusEnum.PENDING.getStatus())
                .originalOrderStatus(order.getStatus())
                .requestedRefundAmount(requestedRefundAmount)
                .needManualRecovery(false)
                .build();
        deltaAfterSaleMapper.insert(afterSale);

        // 8. 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(OperatorTypeEnum.CUSTOMER.getType())
                .operatorId(buyerUserId)
                .operation("提交售后申请")
                .beforeStatus(order.getStatus())
                .afterStatus(ServiceOrderStatusEnum.AFTER_SALE.getStatus())
                .content("买家ID=" + buyerUserId + " 提交售后申请，类型=" + afterSaleType
                        + "，原因：" + reason
                        + (requestedRefundAmount != null ? "，请求退款=" + requestedRefundAmount + "分" : ""))
                .build());

        log.info("售后申请创建成功 afterSaleId={}, serviceOrderId={}", afterSale.getId(), serviceOrderId);
        return afterSale;
    }

    // ======================== 后台批准取消 ========================

    @Transactional(rollbackFor = Exception.class)
    public void approveCancelByAdmin(Long adminUserId, Long cancelId, Integer refundAmount,
                                      Integer responsibilityType, String remark) {
        // 1. 查询取消申请
        DeltaOrderCancelDO cancel = deltaOrderCancelMapper.selectById(cancelId);
        if (cancel == null) throw exception(CANCEL_NOT_EXISTS);
        if (!CancelStatusEnum.isPending(cancel.getApplyStatus())) throw exception(CANCEL_STATUS_INVALID);

        // 2. 查询服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(cancel.getServiceOrderId());
        if (order == null) throw exception(SERVICE_ORDER_NOT_EXISTS);

        // 3. 验证退款金额
        int finalRefund = (refundAmount != null) ? refundAmount : 0;
        if (finalRefund < 0 || finalRefund > order.getServiceAmount()) {
            throw exception(CANCEL_REFUND_AMOUNT_INVALID);
        }

        // 4. CAS 更新服务单：-> CANCELED
        int rows = deltaServiceOrderMapper.updateStatusCas(
                cancel.getServiceOrderId(),
                ServiceOrderStatusEnum.CANCELED.getStatus(),
                order.getStatus(),
                wrapper -> wrapper
                        .set(DeltaServiceOrderDO::getCancelReason, cancel.getApplyReason())
        );
        if (rows != 1) throw exception(SERVICE_ORDER_STATUS_CHANGED);

        LocalDateTime now = LocalDateTime.now();

        // 5. 更新取消申请
        deltaOrderCancelMapper.updateStatusCas(cancelId,
                CancelStatusEnum.APPROVED.getStatus(),
                CancelStatusEnum.PENDING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaOrderCancelDO::getReviewerId, adminUserId)
                        .set(DeltaOrderCancelDO::getReviewTime, now)
                        .set(DeltaOrderCancelDO::getReviewRemark, remark)
                        .set(DeltaOrderCancelDO::getRefundAmount, finalRefund)
                        .set(DeltaOrderCancelDO::getResponsibilityType, responsibilityType)
        );

        // 6. 如果有退款金额，创建待人工退款记录
        if (finalRefund > 0) {
            createRefundRecord(cancel.getServiceOrderId(), null, cancel.getBuyerUserId(),
                    finalRefund, cancel.getApplyReason(), adminUserId, "取消退款");
        }

        // 7. 取消有效分配
        cancelActiveAssignment(cancel.getServiceOrderId());

        // 8. 释放打手状态
        releaseWorkerIfNoActiveOrders(order.getAssignedWorkerId());

        // 9. 处理结算联动：取消未打款的结算
        cancelSettlementIfUnpaid(cancel.getServiceOrderId(), adminUserId);

        // 10. 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(cancel.getServiceOrderId())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .operatorId(adminUserId)
                .operation("批准取消")
                .beforeStatus(order.getStatus())
                .afterStatus(ServiceOrderStatusEnum.CANCELED.getStatus())
                .content("管理员ID=" + adminUserId + " 批准取消，退款=" + finalRefund + "分"
                        + (remark != null ? "，备注：" + remark : ""))
                .build());

        log.info("取消批准成功 cancelId={}, serviceOrderId={}", cancelId, cancel.getServiceOrderId());

        // Phase 10: 发布取消通过事件 -> 通知买家
        try {
            deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.CANCEL_APPROVED.getType())
                    .tenantId(order.getTenantId())
                    .aggregateType("SERVICE_ORDER")
                    .aggregateId(cancel.getServiceOrderId())
                    .bizKey("CANCEL_APPROVED:" + cancelId + ":" + cancel.getBuyerUserId())
                    .recipientId(cancel.getBuyerUserId())
                    .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.CANCEL_APPROVED.getCode())
                    .templateParams(java.util.Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                    .build());
        } catch (Exception e) {
            log.error("取消通过事件写入Outbox失败 cancelId={}", cancelId, e);
        }
    }

    // ======================== 后台驳回取消 ========================

    @Transactional(rollbackFor = Exception.class)
    public void rejectCancelByAdmin(Long adminUserId, Long cancelId, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw exception(CANCEL_REJECT_REASON_EMPTY);

        DeltaOrderCancelDO cancel = deltaOrderCancelMapper.selectById(cancelId);
        if (cancel == null) throw exception(CANCEL_NOT_EXISTS);
        if (!CancelStatusEnum.isPending(cancel.getApplyStatus())) throw exception(CANCEL_STATUS_INVALID);

        // 更新取消申请为驳回
        deltaOrderCancelMapper.updateStatusCas(cancelId,
                CancelStatusEnum.REJECTED.getStatus(),
                CancelStatusEnum.PENDING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaOrderCancelDO::getReviewerId, adminUserId)
                        .set(DeltaOrderCancelDO::getReviewTime, LocalDateTime.now())
                        .set(DeltaOrderCancelDO::getReviewRemark, reason)
        );

        // 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(cancel.getServiceOrderId())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .operatorId(adminUserId)
                .operation("驳回取消申请")
                .beforeStatus(null)
                .afterStatus(null)
                .content("管理员ID=" + adminUserId + " 驳回取消申请，原因：" + reason)
                .build());

        log.info("取消驳回成功 cancelId={}", cancelId);
    }

    // ======================== 后台受理售后 ========================

    @Transactional(rollbackFor = Exception.class)
    public void acceptAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark) {
        DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_EXISTS);
        if (!AfterSaleStatusEnum.isPending(afterSale.getStatus())) throw exception(AFTER_SALE_STATUS_CANNOT_ACCEPT);

        deltaAfterSaleMapper.updateStatusCas(afterSaleId,
                AfterSaleStatusEnum.ACCEPTED.getStatus(),
                AfterSaleStatusEnum.PENDING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaAfterSaleDO::getHandlerId, adminUserId)
                        .set(DeltaAfterSaleDO::getHandleTime, LocalDateTime.now())
                        .set(DeltaAfterSaleDO::getHandleRemark, remark)
        );

        // 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(afterSale.getServiceOrderId())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .operatorId(adminUserId)
                .operation("受理售后案件")
                .beforeStatus(null)
                .afterStatus(null)
                .content("管理员ID=" + adminUserId + " 受理售后案件"
                        + (remark != null ? "，备注：" + remark : ""))
                .build());

        log.info("售后受理成功 afterSaleId={}", afterSaleId);
    }

    // ======================== 后台驳回售后 ========================

    @Transactional(rollbackFor = Exception.class)
    public void rejectAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw exception(AFTER_SALE_REJECT_REASON_EMPTY);

        DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_EXISTS);
        // 只允许待处理或已受理状态驳回
        if (!AfterSaleStatusEnum.isPending(afterSale.getStatus())
                && !AfterSaleStatusEnum.isAccepted(afterSale.getStatus())) {
            throw exception(AFTER_SALE_STATUS_CANNOT_REJECT);
        }

        Integer oldStatus = afterSale.getStatus();

        // 恢复服务单到原状态
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(afterSale.getServiceOrderId());
        if (order != null) {
            int rows = deltaServiceOrderMapper.updateStatusCas(
                    afterSale.getServiceOrderId(),
                    afterSale.getOriginalOrderStatus(),
                    order.getStatus(),
                    wrapper -> {}
            );
            if (rows != 1) throw exception(SERVICE_ORDER_STATUS_CHANGED);
        }

        // 更新售后为驳回
        deltaAfterSaleMapper.updateStatusCas(afterSaleId,
                AfterSaleStatusEnum.REJECTED.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaAfterSaleDO::getHandlerId, adminUserId)
                        .set(DeltaAfterSaleDO::getHandleTime, LocalDateTime.now())
                        .set(DeltaAfterSaleDO::getHandleRemark, reason)
        );

        // 写订单日志
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(afterSale.getServiceOrderId())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .operatorId(adminUserId)
                .operation("驳回售后")
                .beforeStatus(ServiceOrderStatusEnum.AFTER_SALE.getStatus())
                .afterStatus(afterSale.getOriginalOrderStatus())
                .content("管理员ID=" + adminUserId + " 驳回售后，原因：" + reason)
                .build());

        log.info("售后驳回成功 afterSaleId={}, 恢复状态={}", afterSaleId, afterSale.getOriginalOrderStatus());
    }

    // ======================== 后台仲裁售后 ========================

    @Transactional(rollbackFor = Exception.class)
    public void arbitrateAfterSaleByAdmin(Long adminUserId, Long afterSaleId,
                                            Integer decisionType, Integer refundAmount,
                                            Integer responsibilityType,
                                            Integer workerDeductionAmount,
                                            Integer platformBearAmount,
                                            String remark) {
        // 1. 查询售后
        DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_EXISTS);

        // 2. 状态校验
        if (!AfterSaleStatusEnum.isPending(afterSale.getStatus())
                && !AfterSaleStatusEnum.isAccepted(afterSale.getStatus())) {
            throw exception(AFTER_SALE_STATUS_CANNOT_ARBITRATE);
        }

        // 3. 不能重复仲裁
        DeltaAfterSaleArbitrationDO existingArb = deltaAfterSaleArbitrationMapper.selectByAfterSaleId(afterSaleId);
        if (existingArb != null) throw exception(ARBITRATION_ALREADY_EXISTS);

        // 4. 查询服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(afterSale.getServiceOrderId());
        if (order == null) throw exception(SERVICE_ORDER_NOT_EXISTS);

        // 5. 验证金额
        int wdAmount = (workerDeductionAmount != null) ? workerDeductionAmount : 0;
        int pbAmount = (platformBearAmount != null) ? platformBearAmount : 0;

        if (ArbitrationDecisionTypeEnum.FULL_REFUND.getType().equals(decisionType)
                || ArbitrationDecisionTypeEnum.PARTIAL_REFUND.getType().equals(decisionType)) {
            if (refundAmount == null || refundAmount < 0 || refundAmount > order.getServiceAmount()) {
                throw exception(ARBITRATION_AMOUNT_INVALID);
            }
            if (wdAmount + pbAmount > refundAmount) {
                throw exception(ARBITRATION_AMOUNT_SUM_INVALID);
            }
        } else if (ArbitrationDecisionTypeEnum.NO_REFUND.getType().equals(decisionType)
                || ArbitrationDecisionTypeEnum.CONTINUE_SERVICE.getType().equals(decisionType)) {
            refundAmount = 0;
        } else {
            throw exception(ARBITRATION_DECISION_INVALID);
        }

        // 6. 确定服务单新状态
        Integer newOrderStatus;
        if (ArbitrationDecisionTypeEnum.FULL_REFUND.getType().equals(decisionType)) {
            newOrderStatus = ServiceOrderStatusEnum.CANCELED.getStatus();
        } else if (ArbitrationDecisionTypeEnum.PARTIAL_REFUND.getType().equals(decisionType)) {
            // 部分退款：原已完成→COMPLETED，否则→CANCELED
            if (ServiceOrderStatusEnum.isCompleted(afterSale.getOriginalOrderStatus())) {
                newOrderStatus = ServiceOrderStatusEnum.COMPLETED.getStatus();
            } else {
                newOrderStatus = ServiceOrderStatusEnum.CANCELED.getStatus();
            }
        } else {
            // 不退款或继续服务：恢复到原状态
            newOrderStatus = afterSale.getOriginalOrderStatus();
        }

        Integer oldAfterSaleStatus = afterSale.getStatus();

        // 7. CAS 更新服务单状态
        int rows = deltaServiceOrderMapper.updateStatusCas(
                afterSale.getServiceOrderId(), newOrderStatus, order.getStatus(), wrapper -> {});
        if (rows != 1) throw exception(SERVICE_ORDER_STATUS_CHANGED);

        LocalDateTime now = LocalDateTime.now();

        // 8. 保存仲裁记录
        DeltaAfterSaleArbitrationDO arbitration = DeltaAfterSaleArbitrationDO.builder()
                .afterSaleId(afterSaleId)
                .serviceOrderId(afterSale.getServiceOrderId())
                .decisionType(decisionType)
                .refundAmount(refundAmount)
                .workerDeductionAmount(wdAmount)
                .platformBearAmount(pbAmount)
                .responsibilityType(responsibilityType)
                .operatorId(adminUserId)
                .remark(remark)
                .beforeStatus(oldAfterSaleStatus)
                .afterStatus(AfterSaleStatusEnum.ARBITRATED.getStatus())
                .build();
        deltaAfterSaleArbitrationMapper.insert(arbitration);

        // 9. 更新售后状态
        Integer finalRefundAmount = refundAmount;
        deltaAfterSaleMapper.updateStatusCas(afterSaleId,
                AfterSaleStatusEnum.ARBITRATED.getStatus(),
                oldAfterSaleStatus,
                wrapper -> wrapper
                        .set(DeltaAfterSaleDO::getApprovedRefundAmount, finalRefundAmount)
                        .set(DeltaAfterSaleDO::getResponsibilityType, responsibilityType)
                        .set(DeltaAfterSaleDO::getHandlerId, adminUserId)
                        .set(DeltaAfterSaleDO::getHandleTime, now)
                        .set(DeltaAfterSaleDO::getHandleRemark, remark)
        );

        // 10. 如果有退款金额，创建待人工退款记录
        if (refundAmount > 0) {
            createRefundRecord(afterSale.getServiceOrderId(), afterSaleId,
                    afterSale.getBuyerUserId(), refundAmount,
                    afterSale.getReason(), adminUserId, "仲裁退款");
        }

        // 11. 处理结算联动
        if (ArbitrationDecisionTypeEnum.FULL_REFUND.getType().equals(decisionType)) {
            cancelSettlementIfUnpaid(afterSale.getServiceOrderId(), adminUserId);
        }

        // 12. 如果是全额退款或部分退款且进入CANCELED：释放打手
        if (ServiceOrderStatusEnum.isCanceled(newOrderStatus)) {
            releaseWorkerIfNoActiveOrders(order.getAssignedWorkerId());
            cancelActiveAssignment(afterSale.getServiceOrderId());
        }

        // 13. 写订单日志
        String decisionName = decisionType == 1 ? "全额退款" : (decisionType == 2 ? "部分退款"
                : (decisionType == 3 ? "继续服务" : "不退款"));
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(afterSale.getServiceOrderId())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .operatorId(adminUserId)
                .operation("仲裁售后")
                .beforeStatus(order.getStatus())
                .afterStatus(newOrderStatus)
                .content("管理员ID=" + adminUserId + " 仲裁售后，决定=" + decisionName
                        + "，退款=" + refundAmount + "分"
                        + (remark != null ? "，备注：" + remark : ""))
                .build());

        log.info("售后仲裁成功 afterSaleId={}, decisionType={}, refundAmount={}",
                afterSaleId, decisionType, refundAmount);

        // Phase 10: 发布售后仲裁完成事件 -> 通知买家
        try {
            String afterSaleNo = afterSale.getAfterSaleNo();
            String orderNo = afterSaleNo; // fallback
            if (order != null) {
                orderNo = order.getServiceOrderNo();
            }
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("afterSaleNo", afterSaleNo);
            params.put("orderNo", orderNo != null ? orderNo : "");
            deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.AFTER_SALE_ARBITRATED.getType())
                    .tenantId(afterSale.getTenantId())
                    .aggregateType("AFTER_SALE")
                    .aggregateId(afterSaleId)
                    .bizKey("AFTER_SALE_ARBITRATED:" + afterSaleId + ":" + afterSale.getBuyerUserId())
                    .recipientId(afterSale.getBuyerUserId())
                    .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.AFTER_SALE_ARBITRATED.getCode())
                    .templateParams(params)
                    .build());
        } catch (Exception e) {
            log.error("售后仲裁事件写入Outbox失败 afterSaleId={}", afterSaleId, e);
        }
    }

    // ======================== 关闭售后案件 ========================

    @Transactional(rollbackFor = Exception.class)
    public void closeAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark) {
        DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_EXISTS);
        if (AfterSaleStatusEnum.ARBITRATED.getStatus().equals(afterSale.getStatus())
                || AfterSaleStatusEnum.REJECTED.getStatus().equals(afterSale.getStatus())) {
            // 已仲裁或已驳回的可以关闭
            deltaAfterSaleMapper.updateStatusCas(afterSaleId,
                    AfterSaleStatusEnum.CLOSED.getStatus(),
                    afterSale.getStatus(),
                    wrapper -> wrapper
                            .set(DeltaAfterSaleDO::getHandlerId, adminUserId)
                            .set(DeltaAfterSaleDO::getHandleTime, LocalDateTime.now())
                            .set(DeltaAfterSaleDO::getHandleRemark, remark)
            );

            deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                    .serviceOrderId(afterSale.getServiceOrderId())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .operatorId(adminUserId)
                    .operation("关闭售后案件")
                    .beforeStatus(null)
                    .afterStatus(null)
                    .content("管理员ID=" + adminUserId + " 关闭售后案件"
                            + (remark != null ? "，备注：" + remark : ""))
                    .build());

            log.info("售后关闭成功 afterSaleId={}", afterSaleId);
        } else {
            throw exception(AFTER_SALE_STATUS_CANNOT_CLOSE);
        }
    }

    // ======================== 私有辅助方法 ========================

    /**
     * 创建内部退款记录（不调用真实支付接口）
     */
    private void createRefundRecord(Long serviceOrderId, Long afterSaleId, Long buyerUserId,
                                     Integer refundAmount, String refundReason,
                                     Long operatorId, String remark) {
        DeltaRefundRecordDO existing = deltaRefundRecordMapper.selectByAfterSaleId(afterSaleId);
        if (existing != null) {
            log.info("退款记录已存在 afterSaleId={}", afterSaleId);
            return;
        }
        String refundNo = deltaNoRedisDAO.generateRefundNo();
        DeltaRefundRecordDO refund = DeltaRefundRecordDO.builder()
                .refundNo(refundNo)
                .serviceOrderId(serviceOrderId)
                .afterSaleId(afterSaleId)
                .buyerUserId(buyerUserId)
                .refundAmount(refundAmount)
                .refundReason(refundReason)
                .refundStatus(RefundStatusEnum.PENDING_MANUAL.getStatus())
                .operatorId(operatorId)
                .remark(remark)
                .build();
        deltaRefundRecordMapper.insert(refund);
        log.info("内部退款记录创建成功 refundId={}, amount={}", refund.getId(), refundAmount);
    }

    /**
     * 取消有效分配记录
     */
    private void cancelActiveAssignment(Long serviceOrderId) {
        DeltaOrderAssignmentDO assignment = deltaOrderAssignmentMapper.selectActiveByServiceOrderId(serviceOrderId);
        if (assignment != null) {
            assignment.setAssignmentStatus(AssignmentStatusEnum.CANCELED.getStatus());
            deltaOrderAssignmentMapper.updateById(assignment);
            log.info("取消有效分配 assignmentId={}, serviceOrderId={}", assignment.getId(), serviceOrderId);
        }
    }

    /**
     * 释放打手忙碌状态
     */
    private void releaseWorkerIfNoActiveOrders(Long workerId) {
        if (workerId == null) return;

        DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
        if (worker == null) return;
        if (!CommonStatusEnum.isEnable(worker.getStatus())) return;
        if (!WorkerWorkStatusEnum.BUSY.getStatus().equals(worker.getWorkStatus())) return;

        List<DeltaOrderAssignmentDO> activeAssignments = deltaOrderAssignmentMapper
                .selectActiveListByWorkerId(workerId);
        if (activeAssignments.isEmpty()) {
            int rows = deltaWorkerService.getWorkerMapper().updateWorkStatusCas(
                    workerId,
                    WorkerWorkStatusEnum.ONLINE.getStatus(),
                    WorkerWorkStatusEnum.BUSY.getStatus()
            );
            log.info("释放打手状态 workerId={}, BUSY→ONLINE, affected={}", workerId, rows);
        } else {
            log.info("打手仍有{}个有效订单，保持BUSY workerId={}", activeAssignments.size(), workerId);
        }
    }

    /**
     * 取消未打款结算（全额退款/取消批准时）
     */
    private void cancelSettlementIfUnpaid(Long serviceOrderId, Long adminUserId) {
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementMapper.selectByServiceOrderId(serviceOrderId);
        if (settlement == null) return;

        Integer oldStatus = settlement.getSettlementStatus();
        // 已打款：不能自动取消，标记需要人工追回
        if (SettlementStatusEnum.isPaid(oldStatus)) {
            log.warn("已打款结算不能自动取消 serviceOrderId={}, settlementId={}", serviceOrderId, settlement.getId());
            // 标记需要人工追回（在售后记录中）
            DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectActiveByServiceOrderId(serviceOrderId);
            if (afterSale != null) {
                afterSale.setNeedManualRecovery(true);
                deltaAfterSaleMapper.updateById(afterSale);
            }
            return;
        }

        // 待审核/审核通过/审核驳回 → 直接取消
        if (SettlementStatusEnum.isPendingReview(oldStatus)
                || SettlementStatusEnum.isApproved(oldStatus)
                || SettlementStatusEnum.isRejected(oldStatus)) {
            int rows = deltaWorkerSettlementMapper.updateStatusCas(
                    settlement.getId(),
                    SettlementStatusEnum.CANCELED.getStatus(),
                    oldStatus,
                    wrapper -> {}
            );
            if (rows == 1) {
                // 写结算日志
                deltaWorkerSettlementLogService.createLog(DeltaWorkerSettlementLogDO.builder()
                        .settlementId(settlement.getId())
                        .serviceOrderId(serviceOrderId)
                        .operationType(SettlementOperationTypeEnum.GENERATE.getType())
                        .beforeStatus(oldStatus)
                        .afterStatus(SettlementStatusEnum.CANCELED.getStatus())
                        .operatorType(OperatorTypeEnum.ADMIN.getType())
                        .operatorId(adminUserId)
                        .content("取消/全额退款导致结算取消，管理员ID=" + adminUserId)
                        .build());
                log.info("取消未打款结算 settlementId={}, oldStatus={}", settlement.getId(), oldStatus);
            }
        }
    }

}
