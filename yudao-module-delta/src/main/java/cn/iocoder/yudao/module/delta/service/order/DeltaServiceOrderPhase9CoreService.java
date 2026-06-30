package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.*;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Phase 9 人工退款执行与追回账务闭环 核心事务 Service
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaServiceOrderPhase9CoreService {

    /** 外部参考号最大长度 */
    private static final int MAX_EXTERNAL_REF_LENGTH = 128;

    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaRefundLogMapper deltaRefundLogMapper;
    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;
    @Resource
    private DeltaFundRecoveryLogMapper deltaFundRecoveryLogMapper;
    @Resource
    private DeltaAfterSaleMapper deltaAfterSaleMapper;
    @Resource
    private DeltaAfterSaleArbitrationMapper deltaAfterSaleArbitrationMapper;
    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;

    // ======================== 人工退款执行 ========================

    /**
     * 管理员开始处理退款
     */
    @Transactional(rollbackFor = Exception.class)
    public void startRefund(Long adminUserId, Long refundId, String remark) {
        // 1. 查询退款记录
        DeltaRefundRecordDO refund = deltaRefundRecordMapper.selectById(refundId);
        if (refund == null) throw exception(REFUND_RECORD_NOT_EXISTS);

        // 2. 当前状态必须为待人工处理
        if (!RefundStatusEnum.isPendingManual(refund.getRefundStatus())) {
            throw exception(REFUND_STATUS_CANNOT_START);
        }

        LocalDateTime now = LocalDateTime.now();

        // 3. CAS 更新为处理中
        int rows = deltaRefundRecordMapper.updateStatusCas(refundId,
                RefundStatusEnum.MANUAL_PROCESSING.getStatus(),
                RefundStatusEnum.PENDING_MANUAL.getStatus(),
                wrapper -> wrapper
                        .set(DeltaRefundRecordDO::getHandlerId, adminUserId)
                        .set(DeltaRefundRecordDO::getHandleTime, now)
                        .set(DeltaRefundRecordDO::getProcessRemark, remark)
        );
        if (rows != 1) throw exception(REFUND_STATUS_CHANGED);

        // 4. 写退款日志
        writeRefundLog(refund, RefundLogOperationTypeEnum.START.getType(),
                OperatorTypeEnum.ADMIN.name(), adminUserId,
                "开始处理退款，备注：" + (remark != null ? remark : "无"));

        log.info("退款开始处理 refundId={}, handlerId={}", refundId, adminUserId);
    }

    /**
     * 管理员标记退款完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeRefund(Long adminUserId, Long refundId, Integer refundMethod,
                                String externalReference, String proofUrls, String remark) {
        // 1. 查询退款记录
        DeltaRefundRecordDO refund = deltaRefundRecordMapper.selectById(refundId);
        if (refund == null) throw exception(REFUND_RECORD_NOT_EXISTS);

        // 2. 状态必须为处理中
        if (!RefundStatusEnum.isManualProcessing(refund.getRefundStatus())) {
            throw exception(REFUND_STATUS_CANNOT_COMPLETE);
        }

        // 3. 退款方式必须合法
        if (refundMethod == null || !RefundMethodEnum.isValid(refundMethod)) {
            throw exception(REFUND_METHOD_INVALID);
        }

        // 4. 外部参考号长度校验
        if (externalReference != null && externalReference.length() > MAX_EXTERNAL_REF_LENGTH) {
            throw exception(REFUND_EXTERNAL_REF_TOO_LONG);
        }

        // 5. 至少填写外部参考号、凭证或备注之一
        if ((externalReference == null || externalReference.trim().isEmpty())
                && (proofUrls == null || proofUrls.trim().isEmpty())
                && (remark == null || remark.trim().isEmpty())) {
            throw exception(REFUND_PROOF_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();

        // 6. CAS 更新为已完成
        int rows = deltaRefundRecordMapper.updateStatusCas(refundId,
                RefundStatusEnum.MANUAL_COMPLETED.getStatus(),
                RefundStatusEnum.MANUAL_PROCESSING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaRefundRecordDO::getRefundMethod, refundMethod)
                        .set(DeltaRefundRecordDO::getExternalReference, externalReference)
                        .set(DeltaRefundRecordDO::getProofUrls, proofUrls)
                        .set(DeltaRefundRecordDO::getCompletedTime, now)
                        .set(DeltaRefundRecordDO::getOperatorId, adminUserId)
                        .set(DeltaRefundRecordDO::getProcessRemark, remark)
        );
        if (rows != 1) throw exception(REFUND_STATUS_CHANGED);

        // 7. 写退款日志
        String logContent = "退款完成，方式=" + refundMethod
                + (externalReference != null ? "，外部参考号=" + externalReference : "")
                + (remark != null ? "，备注：" + remark : "");
        writeRefundLog(refund, RefundLogOperationTypeEnum.COMPLETE.getType(),
                OperatorTypeEnum.ADMIN.name(), adminUserId, logContent);

        log.info("退款完成 refundId={}, method={}, handlerId={}", refundId, refundMethod, adminUserId);

        // Phase 10: 发布退款完成事件 -> 通知买家
        try {
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(refund.getServiceOrderId());
            if (order != null && refund.getBuyerUserId() != null) {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("refundNo", refund.getRefundNo());
                params.put("orderNo", order.getServiceOrderNo());
                params.put("amount", String.valueOf(refund.getRefundAmount()));
                deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                        .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.REFUND_COMPLETED.getType())
                        .tenantId(refund.getTenantId())
                        .aggregateType("REFUND")
                        .aggregateId(refundId)
                        .bizKey("REFUND_COMPLETED:" + refundId + ":" + refund.getBuyerUserId())
                        .recipientId(refund.getBuyerUserId())
                        .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.REFUND_COMPLETED.getCode())
                        .templateParams(params)
                        .build());
            }
        } catch (Exception e) {
            log.error("退款完成事件写入Outbox失败 refundId={}", refundId, e);
        }
    }

    /**
     * 管理员标记退款失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void failRefund(Long adminUserId, Long refundId, String reason) {
        // 1. 参数校验
        if (reason == null || reason.trim().isEmpty()) throw exception(REFUND_FAIL_REASON_REQUIRED);

        // 2. 查询退款记录
        DeltaRefundRecordDO refund = deltaRefundRecordMapper.selectById(refundId);
        if (refund == null) throw exception(REFUND_RECORD_NOT_EXISTS);

        // 3. 状态必须为处理中
        if (!RefundStatusEnum.isManualProcessing(refund.getRefundStatus())) {
            throw exception(REFUND_STATUS_CANNOT_FAIL);
        }

        LocalDateTime now = LocalDateTime.now();

        // 4. CAS 更新为失败
        int rows = deltaRefundRecordMapper.updateStatusCas(refundId,
                RefundStatusEnum.MANUAL_FAILED.getStatus(),
                RefundStatusEnum.MANUAL_PROCESSING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaRefundRecordDO::getFailedTime, now)
                        .set(DeltaRefundRecordDO::getFailureReason, reason)
        );
        if (rows != 1) throw exception(REFUND_STATUS_CHANGED);

        // 5. 写退款日志
        writeRefundLog(refund, RefundLogOperationTypeEnum.FAIL.getType(),
                OperatorTypeEnum.ADMIN.name(), adminUserId,
                "退款失败，原因：" + reason);

        log.info("退款标记失败 refundId={}, reason={}", refundId, reason);

        // Phase 10: 发布退款失败事件 -> 通知买家
        try {
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(refund.getServiceOrderId());
            if (order != null && refund.getBuyerUserId() != null) {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("refundNo", refund.getRefundNo());
                params.put("orderNo", order.getServiceOrderNo());
                deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                        .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.REFUND_FAILED.getType())
                        .tenantId(refund.getTenantId())
                        .aggregateType("REFUND")
                        .aggregateId(refundId)
                        .bizKey("REFUND_FAILED:" + refundId + ":" + refund.getBuyerUserId())
                        .recipientId(refund.getBuyerUserId())
                        .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.REFUND_FAILED.getCode())
                        .templateParams(params)
                        .build());
            }
        } catch (Exception e) {
            log.error("退款失败事件写入Outbox失败 refundId={}", refundId, e);
        }
    }

    /**
     * 管理员重新处理退款（从失败回到待处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryRefund(Long adminUserId, Long refundId, String remark) {
        // 1. 查询退款记录
        DeltaRefundRecordDO refund = deltaRefundRecordMapper.selectById(refundId);
        if (refund == null) throw exception(REFUND_RECORD_NOT_EXISTS);

        // 2. 只能重试已失败的
        if (!RefundStatusEnum.isManualFailed(refund.getRefundStatus())) {
            throw exception(REFUND_STATUS_CANNOT_RETRY);
        }

        // 3. CAS 恢复到待人工处理
        int rows = deltaRefundRecordMapper.updateStatusCas(refundId,
                RefundStatusEnum.PENDING_MANUAL.getStatus(),
                RefundStatusEnum.MANUAL_FAILED.getStatus(),
                wrapper -> wrapper
                        .set(DeltaRefundRecordDO::getFailedTime, null)
                        .set(DeltaRefundRecordDO::getFailureReason, null)
                        .set(DeltaRefundRecordDO::getHandlerId, null)
                        .set(DeltaRefundRecordDO::getHandleTime, null)
                        .set(DeltaRefundRecordDO::getProcessRemark, remark)
        );
        if (rows != 1) throw exception(REFUND_STATUS_CHANGED);

        // 4. 写退款日志
        writeRefundLog(refund, RefundLogOperationTypeEnum.RETRY.getType(),
                OperatorTypeEnum.ADMIN.name(), adminUserId,
                "重新处理退款" + (remark != null ? "，备注：" + remark : ""));

        log.info("退款重新处理 refundId={}", refundId);
    }

    /**
     * 管理员撤销退款记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelRefund(Long adminUserId, Long refundId, String reason) {
        // 1. 参数校验
        if (reason == null || reason.trim().isEmpty()) throw exception(RECOVERY_CANCEL_REASON_REQUIRED);

        // 2. 查询退款记录
        DeltaRefundRecordDO refund = deltaRefundRecordMapper.selectById(refundId);
        if (refund == null) throw exception(REFUND_RECORD_NOT_EXISTS);

        // 3. 只能撤销待人工处理的
        if (!RefundStatusEnum.isPendingManual(refund.getRefundStatus())) {
            throw exception(REFUND_STATUS_CANNOT_CANCEL);
        }

        // 4. CAS 更新为已取消
        int rows = deltaRefundRecordMapper.updateStatusCas(refundId,
                RefundStatusEnum.CANCELED.getStatus(),
                RefundStatusEnum.PENDING_MANUAL.getStatus(),
                wrapper -> wrapper
                        .set(DeltaRefundRecordDO::getProcessRemark, reason)
        );
        if (rows != 1) throw exception(REFUND_STATUS_CHANGED);

        // 5. 写退款日志
        writeRefundLog(refund, RefundLogOperationTypeEnum.CANCEL.getType(),
                OperatorTypeEnum.ADMIN.name(), adminUserId,
                "撤销退款记录，原因：" + reason);

        log.info("退款记录撤销 refundId={}, reason={}", refundId, reason);
    }

    // ======================== 追回任务 ========================

    /**
     * 生成追回任务（通常由仲裁时自动调用，也可后台手动补生成）
     */
    @Transactional(rollbackFor = Exception.class)
    public DeltaFundRecoveryDO createRecoveryTask(Long adminUserId, Long afterSaleId) {
        // 1. 查询售后和仲裁
        DeltaAfterSaleDO afterSale = deltaAfterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_EXISTS);

        DeltaAfterSaleArbitrationDO arbitration = deltaAfterSaleArbitrationMapper.selectByAfterSaleId(afterSaleId);
        if (arbitration == null) throw exception(ARBITRATION_ALREADY_EXISTS); // 复用：未仲裁不能生成

        // 2. 只需要追回金额大于0
        if (arbitration.getWorkerDeductionAmount() == null || arbitration.getWorkerDeductionAmount() <= 0) {
            log.info("打手扣减金额为0，不生成追回任务 afterSaleId={}", afterSaleId);
            return null;
        }

        // 3. 查询结算
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementMapper.selectByServiceOrderId(
                afterSale.getServiceOrderId());
        if (settlement == null) {
            log.info("无结算记录，不生成追回任务 afterSaleId={}", afterSaleId);
            return null;
        }

        // 4. 只有已打款结算才需要追回
        if (!cn.iocoder.yudao.module.delta.enums.settlement.SettlementStatusEnum.isPaid(
                settlement.getSettlementStatus())) {
            log.info("结算未打款，不生成追回任务 afterSaleId={}, settlementStatus={}",
                    afterSaleId, settlement.getSettlementStatus());
            return null;
        }

        // 5. 同一仲裁只能有一个追回任务
        DeltaFundRecoveryDO existing = deltaFundRecoveryMapper.selectByArbitrationId(arbitration.getId());
        if (existing != null) throw exception(RECOVERY_ALREADY_EXISTS);

        // 6. 创建追回任务
        int shouldRecoverAmount = arbitration.getWorkerDeductionAmount();
        String recoveryNo = deltaNoRedisDAO.generateFundRecoveryNo();
        DeltaFundRecoveryDO recovery = DeltaFundRecoveryDO.builder()
                .recoveryNo(recoveryNo)
                .serviceOrderId(afterSale.getServiceOrderId())
                .afterSaleId(afterSaleId)
                .arbitrationId(arbitration.getId())
                .settlementId(settlement.getId())
                .workerId(settlement.getWorkerId())
                .responsibilityType(arbitration.getResponsibilityType())
                .shouldRecoverAmount(shouldRecoverAmount)
                .recoveredAmount(0)
                .remainingAmount(shouldRecoverAmount)
                .recoveryStatus(RecoveryStatusEnum.PENDING.getStatus())
                .build();
        deltaFundRecoveryMapper.insert(recovery);

        // 7. 写追回日志
        writeRecoveryLog(recovery, RecoveryOperationTypeEnum.GENERATE.getType(), adminUserId,
                "生成追回任务，应追回金额=" + shouldRecoverAmount + "分", 0);

        log.info("追回任务生成 recoveryId={}, shouldRecoverAmount={}", recovery.getId(), shouldRecoverAmount);

        // Phase 10: 发布追回任务生成事件 -> 通知后台
        try {
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(recovery.getServiceOrderId());
            if (order != null) {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("recoveryNo", recoveryNo);
                params.put("orderNo", order.getServiceOrderNo());
                params.put("amount", String.valueOf(shouldRecoverAmount));
                deltaEventPublisher.publishToAdmin(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                        .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.RECOVERY_CREATED.getType())
                        .tenantId(recovery.getTenantId())
                        .aggregateType("FUND_RECOVERY")
                        .aggregateId(recovery.getId())
                        .bizKey("RECOVERY_CREATED:" + recovery.getId() + ":ADMIN")
                        .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.RECOVERY_NEEDED.getCode())
                        .templateParams(params)
                        .build());
            }
        } catch (Exception e) {
            log.error("追回任务生成事件写入Outbox失败 recoveryId={}", recovery.getId(), e);
        }

        return recovery;
    }

    /**
     * 管理员开始追回
     */
    @Transactional(rollbackFor = Exception.class)
    public void startRecovery(Long adminUserId, Long recoveryId, String remark) {
        // 1. 查询追回任务
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(recoveryId);
        if (recovery == null) throw exception(RECOVERY_NOT_EXISTS);

        // 2. 只能从待处理开始
        if (!RecoveryStatusEnum.isPending(recovery.getRecoveryStatus())) {
            throw exception(RECOVERY_STATUS_CANNOT_START);
        }

        // 3. CAS 更新为处理中
        int rows = deltaFundRecoveryMapper.updateStatusCas(recoveryId,
                RecoveryStatusEnum.PROCESSING.getStatus(),
                RecoveryStatusEnum.PENDING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaFundRecoveryDO::getHandlerId, adminUserId)
                        .set(DeltaFundRecoveryDO::getHandleTime, LocalDateTime.now())
                        .set(DeltaFundRecoveryDO::getRemark, remark)
        );
        if (rows != 1) throw exception(RECOVERY_STATUS_CANNOT_START);

        // 4. 写追回日志
        writeRecoveryLog(recovery, RecoveryOperationTypeEnum.START.getType(), adminUserId,
                "开始追回" + (remark != null ? "，备注：" + remark : ""), 0);

        log.info("追回开始 recoveryId={}, handlerId={}", recoveryId, adminUserId);
    }

    /**
     * 管理员记录追回结果（支持部分追回和全部追回）
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordRecovery(Long adminUserId, Long recoveryId, Integer recoveredAmount,
                                Integer recoveryMethod, String externalReference,
                                String proofUrls, String remark) {
        // 1. 参数校验
        if (recoveredAmount == null || recoveredAmount <= 0) throw exception(RECOVERY_AMOUNT_MUST_POSITIVE);

        // 2. 查询追回任务
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(recoveryId);
        if (recovery == null) throw exception(RECOVERY_NOT_EXISTS);

        // 3. 状态必须在处理中或部分追回
        if (!RecoveryStatusEnum.isProcessing(recovery.getRecoveryStatus())
                && !RecoveryStatusEnum.isPartiallyRecovered(recovery.getRecoveryStatus())) {
            throw exception(RECOVERY_STATUS_CANNOT_RECORD);
        }

        // 4. 计算累计金额
        int newRecoveredAmount = recovery.getRecoveredAmount() + recoveredAmount;
        int newRemaining = recovery.getShouldRecoverAmount() - newRecoveredAmount;

        // 5. 累计不能超过应追回金额
        if (newRecoveredAmount > recovery.getShouldRecoverAmount()) {
            throw exception(RECOVERY_AMOUNT_EXCEED);
        }

        // 6. 决定新状态
        Integer newStatus;
        String logOpType;
        if (newRecoveredAmount >= recovery.getShouldRecoverAmount()) {
            newStatus = RecoveryStatusEnum.RECOVERED.getStatus();
            logOpType = RecoveryOperationTypeEnum.RECORD_COMPLETE.getType();
        } else {
            newStatus = RecoveryStatusEnum.PARTIALLY_RECOVERED.getStatus();
            logOpType = RecoveryOperationTypeEnum.RECORD_PARTIAL.getType();
        }

        LocalDateTime now = LocalDateTime.now();

        // 7. CAS 更新状态和金额
        int rows = deltaFundRecoveryMapper.updateStatusCasWithAmount(recoveryId, newStatus,
                recovery.getRecoveryStatus(), newRecoveredAmount, newRemaining,
                wrapper -> {
                    wrapper.set(DeltaFundRecoveryDO::getRecoveryMethod, recoveryMethod)
                            .set(DeltaFundRecoveryDO::getExternalReference, externalReference)
                            .set(DeltaFundRecoveryDO::getProofUrls, proofUrls)
                            .set(DeltaFundRecoveryDO::getHandlerId, adminUserId);
                    if (RecoveryStatusEnum.RECOVERED.getStatus().equals(newStatus)) {
                        wrapper.set(DeltaFundRecoveryDO::getCompletedTime, now);
                    }
                    if (remark != null) {
                        wrapper.set(DeltaFundRecoveryDO::getRemark, remark);
                    }
                }
        );
        if (rows != 1) throw exception(RECOVERY_STATUS_CANNOT_RECORD);

        // 8. 写追回日志
        String content = "记录追回金额=" + recoveredAmount + "分，累计=" + newRecoveredAmount
                + "分，剩余=" + newRemaining + "分";
        if (remark != null) content += "，备注：" + remark;
        writeRecoveryLog(recovery, logOpType, adminUserId, content, recoveredAmount);

        log.info("追回记录 recoveryId={}, amount={}, totalRecovered={}, remaining={}",
                recoveryId, recoveredAmount, newRecoveredAmount, newRemaining);
    }

    /**
     * 管理员标记追回失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void failRecovery(Long adminUserId, Long recoveryId, String reason) {
        // 1. 参数校验
        if (reason == null || reason.trim().isEmpty()) throw exception(RECOVERY_FAIL_REASON_REQUIRED);

        // 2. 查询追回任务
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(recoveryId);
        if (recovery == null) throw exception(RECOVERY_NOT_EXISTS);

        // 3. 允许处理中和部分追回进入失败
        if (!RecoveryStatusEnum.isProcessing(recovery.getRecoveryStatus())
                && !RecoveryStatusEnum.isPartiallyRecovered(recovery.getRecoveryStatus())) {
            throw exception(RECOVERY_STATUS_CANNOT_FAIL);
        }

        // 4. CAS 更新为失败（不清零已追回金额）
        int rows = deltaFundRecoveryMapper.updateStatusCas(recoveryId,
                RecoveryStatusEnum.FAILED.getStatus(),
                recovery.getRecoveryStatus(),
                wrapper -> wrapper
                        .set(DeltaFundRecoveryDO::getFailureReason, reason)
        );
        if (rows != 1) throw exception(RECOVERY_STATUS_CANNOT_FAIL);

        // 5. 写追回日志
        writeRecoveryLog(recovery, RecoveryOperationTypeEnum.FAIL.getType(), adminUserId,
                "追回失败，原因：" + reason + "，已追回=" + recovery.getRecoveredAmount() + "分", 0);

        log.info("追回标记失败 recoveryId={}, reason={}", recoveryId, reason);
    }

    /**
     * 管理员重试追回（失败→待处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryRecovery(Long adminUserId, Long recoveryId, String remark) {
        // 1. 查询追回任务
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(recoveryId);
        if (recovery == null) throw exception(RECOVERY_NOT_EXISTS);

        // 2. 只能重试已失败的
        if (!RecoveryStatusEnum.isFailed(recovery.getRecoveryStatus())) {
            throw exception(RECOVERY_STATUS_CANNOT_RETRY);
        }

        // 3. CAS 恢复到待处理
        int rows = deltaFundRecoveryMapper.updateStatusCas(recoveryId,
                RecoveryStatusEnum.PENDING.getStatus(),
                RecoveryStatusEnum.FAILED.getStatus(),
                wrapper -> wrapper
                        .set(DeltaFundRecoveryDO::getFailureReason, null)
                        .set(DeltaFundRecoveryDO::getHandlerId, null)
                        .set(DeltaFundRecoveryDO::getHandleTime, null)
                        .set(DeltaFundRecoveryDO::getRemark, remark)
        );
        if (rows != 1) throw exception(RECOVERY_STATUS_CANNOT_RETRY);

        // 4. 写追回日志
        writeRecoveryLog(recovery, RecoveryOperationTypeEnum.RETRY.getType(), adminUserId,
                "重试追回，已追回保留=" + recovery.getRecoveredAmount() + "分"
                        + (remark != null ? "，备注：" + remark : ""), 0);

        log.info("追回重试 recoveryId={}", recoveryId);
    }

    /**
     * 管理员取消追回任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelRecovery(Long adminUserId, Long recoveryId, String reason) {
        // 1. 参数校验
        if (reason == null || reason.trim().isEmpty()) throw exception(RECOVERY_CANCEL_REASON_REQUIRED);

        // 2. 查询追回任务
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(recoveryId);
        if (recovery == null) throw exception(RECOVERY_NOT_EXISTS);

        // 3. 只能取消待处理的
        if (!RecoveryStatusEnum.isPending(recovery.getRecoveryStatus())) {
            throw exception(RECOVERY_STATUS_CANNOT_CANCEL);
        }

        // 4. 已有追回金额不能取消
        if (recovery.getRecoveredAmount() > 0) throw exception(RECOVERY_HAS_AMOUNT_CANNOT_CANCEL);

        // 5. CAS 更新为已取消
        int rows = deltaFundRecoveryMapper.updateStatusCas(recoveryId,
                RecoveryStatusEnum.CANCELED.getStatus(),
                RecoveryStatusEnum.PENDING.getStatus(),
                wrapper -> wrapper
                        .set(DeltaFundRecoveryDO::getRemark, reason)
        );
        if (rows != 1) throw exception(RECOVERY_STATUS_CANNOT_CANCEL);

        // 6. 写追回日志
        writeRecoveryLog(recovery, RecoveryOperationTypeEnum.CANCEL.getType(), adminUserId,
                "取消追回任务，原因：" + reason, 0);

        log.info("追回任务取消 recoveryId={}, reason={}", recoveryId, reason);
    }

    // ======================== 私有辅助方法 ========================

    /**
     * 写退款操作日志
     */
    private void writeRefundLog(DeltaRefundRecordDO refund, String operationType,
                                 String operatorType, Long operatorId, String content) {
        DeltaRefundLogDO logDO = DeltaRefundLogDO.builder()
                .refundRecordId(refund.getId())
                .serviceOrderId(refund.getServiceOrderId())
                .afterSaleId(refund.getAfterSaleId())
                .operationType(operationType)
                .beforeStatus(refund.getRefundStatus())
                .afterStatus(refund.getRefundStatus())
                .operatorType(operatorType)
                .operatorId(operatorId)
                .content(content)
                .amountSnapshot(refund.getRefundAmount())
                .build();
        deltaRefundLogMapper.insert(logDO);
    }

    /**
     * 写追回操作日志
     */
    private void writeRecoveryLog(DeltaFundRecoveryDO recovery, String operationType,
                                   Long operatorId, String content, Integer thisAmount) {
        DeltaFundRecoveryLogDO logDO = DeltaFundRecoveryLogDO.builder()
                .recoveryId(recovery.getId())
                .serviceOrderId(recovery.getServiceOrderId())
                .settlementId(recovery.getSettlementId())
                .operationType(operationType)
                .beforeStatus(recovery.getRecoveryStatus())
                .afterStatus(recovery.getRecoveryStatus())
                .operatorId(operatorId)
                .content(content)
                .amount(thisAmount)
                .totalRecoveredAmount(recovery.getRecoveredAmount())
                .remainingAmount(recovery.getRemainingAmount())
                .build();
        deltaFundRecoveryLogMapper.insert(logDO);
    }

}
