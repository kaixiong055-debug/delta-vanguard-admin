package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementStatusEnum;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Phase 7 结算生成与审核核心事务 Service（独立 Bean 确保 @Transactional 生效）
 * <p>
 * 抽成比例为万分制：1500 = 15.00%
 * 金额计算：platformFee = serviceAmount × commissionRate / 10000，四舍五入到分
 * workerAmount = serviceAmount - platformFee
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaWorkerSettlementPhase7CoreService {

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaWorkerService deltaWorkerService;
    @Resource
    private DeltaWorkerSettlementService deltaWorkerSettlementService;
    @Resource
    private DeltaWorkerSettlementLogService deltaWorkerSettlementLogService;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;

    // ========== 生成结算（验收时调用） ==========

    /**
     * 为已完成的订单生成结算单（验收事务内调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public DeltaWorkerSettlementDO createSettlementForCompletedOrder(Long serviceOrderId) {
        return doCreateSettlement(serviceOrderId, OperatorTypeEnum.SYSTEM.getType(), 0L);
    }

    // ========== 后台人工补生成结算 ==========

    @Transactional(rollbackFor = Exception.class)
    public DeltaWorkerSettlementDO generateSettlementByAdmin(Long adminUserId, Long serviceOrderId) {
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }
        // 服务单必须已完成
        if (!cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum.isCompleted(order.getStatus())) {
            throw exception(SETTLEMENT_SERVICE_ORDER_NOT_COMPLETED);
        }
        // 幂等：已存在则返回
        DeltaWorkerSettlementDO existing = deltaWorkerSettlementService.getSettlementByServiceOrderId(serviceOrderId);
        if (existing != null) {
            return existing;
        }
        return doCreateSettlement(serviceOrderId, OperatorTypeEnum.ADMIN.getType(), adminUserId);
    }

    // ========== 审核通过 ==========

    @Transactional(rollbackFor = Exception.class)
    public void approveSettlement(Long adminUserId, Long settlementId, String remark) {
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(settlementId);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        Integer oldStatus = settlement.getSettlementStatus();
        if (!SettlementStatusEnum.isPendingReview(oldStatus)) {
            throw exception(SETTLEMENT_CANNOT_APPROVE);
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = deltaWorkerSettlementService.getMapper().updateStatusCas(
                settlementId,
                SettlementStatusEnum.APPROVED.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaWorkerSettlementDO::getReviewerId, adminUserId)
                        .set(DeltaWorkerSettlementDO::getReviewTime, now)
                        .set(DeltaWorkerSettlementDO::getRemark, remark)
        );
        if (rows != 1) {
            throw exception(SETTLEMENT_STATUS_CHANGED);
        }

        // 写结算日志
        writeSettlementLog(settlementId, settlement.getServiceOrderId(),
                SettlementOperationTypeEnum.APPROVE.getType(),
                oldStatus, SettlementStatusEnum.APPROVED.getStatus(),
                OperatorTypeEnum.ADMIN.getType(), adminUserId,
                "管理员ID=" + adminUserId + " 审核通过" + (remark != null ? "，备注：" + remark : ""));

        // Phase 10: 发布结算通过事件 -> 通知打手
        publishSettlementEvent(settlement, cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SETTLEMENT_APPROVED,
                cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SETTLEMENT_APPROVED);
    }

    // ========== 审核驳回 ==========

    @Transactional(rollbackFor = Exception.class)
    public void rejectSettlement(Long adminUserId, Long settlementId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(SETTLEMENT_REJECT_REASON_EMPTY);
        }

        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(settlementId);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        Integer oldStatus = settlement.getSettlementStatus();
        if (!SettlementStatusEnum.isPendingReview(oldStatus)) {
            throw exception(SETTLEMENT_CANNOT_REJECT);
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = deltaWorkerSettlementService.getMapper().updateStatusCas(
                settlementId,
                SettlementStatusEnum.REJECTED.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaWorkerSettlementDO::getReviewerId, adminUserId)
                        .set(DeltaWorkerSettlementDO::getReviewTime, now)
                        .set(DeltaWorkerSettlementDO::getRejectReason, reason)
        );
        if (rows != 1) {
            throw exception(SETTLEMENT_STATUS_CHANGED);
        }

        writeSettlementLog(settlementId, settlement.getServiceOrderId(),
                SettlementOperationTypeEnum.REJECT.getType(),
                oldStatus, SettlementStatusEnum.REJECTED.getStatus(),
                OperatorTypeEnum.ADMIN.getType(), adminUserId,
                "管理员ID=" + adminUserId + " 审核驳回，原因：" + reason);

        // Phase 10: 发布结算驳回事件 -> 通知打手
        publishSettlementEventWithReason(settlement, cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SETTLEMENT_REJECTED,
                cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SETTLEMENT_REJECTED, reason);
    }

    // ========== 重新提交审核 ==========

    @Transactional(rollbackFor = Exception.class)
    public void resubmitSettlement(Long adminUserId, Long settlementId, Integer commissionRate, String remark) {
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(settlementId);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        Integer oldStatus = settlement.getSettlementStatus();
        if (!SettlementStatusEnum.isRejected(oldStatus)) {
            throw exception(SETTLEMENT_CANNOT_RESUBMIT);
        }

        // 允许修改抽成比例并重新计算金额
        final Integer finalCommissionRate = (commissionRate != null) ? commissionRate : settlement.getCommissionRate();
        if (finalCommissionRate == null || finalCommissionRate < 0 || finalCommissionRate > 10000) {
            throw exception(SETTLEMENT_COMMISSION_RATE_INVALID);
        }

        // 重新计算金额
        int serviceAmount = settlement.getServiceAmount();
        long platformFeeL = Math.round((long) serviceAmount * finalCommissionRate / 10000.0);
        int platformFee = (int) platformFeeL;
        int workerAmount = serviceAmount - platformFee;

        // 记录金额快照JSON
        String amountSnapshot = String.format("{\"serviceAmount\":%d,\"commissionRate\":%d,\"platformFee\":%d,\"workerAmount\":%d}",
                serviceAmount, finalCommissionRate, platformFee, workerAmount);

        int rows = deltaWorkerSettlementService.getMapper().updateStatusCas(
                settlementId,
                SettlementStatusEnum.PENDING_REVIEW.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaWorkerSettlementDO::getCommissionRate, finalCommissionRate)
                        .set(DeltaWorkerSettlementDO::getPlatformFee, platformFee)
                        .set(DeltaWorkerSettlementDO::getWorkerAmount, workerAmount)
                        .set(DeltaWorkerSettlementDO::getRejectReason, null)
                        .set(DeltaWorkerSettlementDO::getReviewerId, null)
                        .set(DeltaWorkerSettlementDO::getReviewTime, null)
        );
        if (rows != 1) {
            throw exception(SETTLEMENT_STATUS_CHANGED);
        }

        writeSettlementLog(settlementId, settlement.getServiceOrderId(),
                SettlementOperationTypeEnum.RESUBMIT.getType(),
                oldStatus, SettlementStatusEnum.PENDING_REVIEW.getStatus(),
                OperatorTypeEnum.ADMIN.getType(), adminUserId,
                "管理员ID=" + adminUserId + " 重新提交审核，抽成比例=" + finalCommissionRate
                        + (remark != null ? "，备注：" + remark : ""),
                amountSnapshot);
    }

    // ========== 标记已打款 ==========

    @Transactional(rollbackFor = Exception.class)
    public void markSettlementPaid(Long adminUserId, Long settlementId, Integer payMethod,
                                    String payReference, String remark) {
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(settlementId);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        Integer oldStatus = settlement.getSettlementStatus();
        if (!SettlementStatusEnum.isApproved(oldStatus)) {
            throw exception(SETTLEMENT_CANNOT_MARK_PAID);
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = deltaWorkerSettlementService.getMapper().updateStatusCas(
                settlementId,
                SettlementStatusEnum.PAID.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaWorkerSettlementDO::getPayerId, adminUserId)
                        .set(DeltaWorkerSettlementDO::getPaidTime, now)
                        .set(DeltaWorkerSettlementDO::getPayMethod, payMethod)
                        .set(DeltaWorkerSettlementDO::getPayReference, payReference)
                        .set(DeltaWorkerSettlementDO::getPayRemark, remark)
        );
        if (rows != 1) {
            throw exception(SETTLEMENT_STATUS_CHANGED);
        }

        writeSettlementLog(settlementId, settlement.getServiceOrderId(),
                SettlementOperationTypeEnum.MARK_PAID.getType(),
                oldStatus, SettlementStatusEnum.PAID.getStatus(),
                OperatorTypeEnum.ADMIN.getType(), adminUserId,
                "管理员ID=" + adminUserId + " 标记已打款，方式=" + payMethod
                        + (payReference != null ? "，参考号=" + payReference : "")
                        + (remark != null ? "，备注：" + remark : ""));

        // Phase 10: 发布结算已打款事件 -> 通知打手
        publishSettlementEventWithAmount(settlement, cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SETTLEMENT_PAID,
                cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SETTLEMENT_PAID);
    }

    // ========== 撤销打款标记 ==========

    @Transactional(rollbackFor = Exception.class)
    public void revokeSettlementPaid(Long adminUserId, Long settlementId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw exception(SETTLEMENT_REVOKE_REASON_EMPTY);
        }

        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(settlementId);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        Integer oldStatus = settlement.getSettlementStatus();
        if (!SettlementStatusEnum.isPaid(oldStatus)) {
            throw exception(SETTLEMENT_CANNOT_REVOKE_PAID);
        }

        int rows = deltaWorkerSettlementService.getMapper().updateStatusCas(
                settlementId,
                SettlementStatusEnum.APPROVED.getStatus(),
                oldStatus,
                wrapper -> wrapper
                        .set(DeltaWorkerSettlementDO::getPayerId, null)
                        .set(DeltaWorkerSettlementDO::getPaidTime, null)
                        .set(DeltaWorkerSettlementDO::getPayMethod, null)
                        .set(DeltaWorkerSettlementDO::getPayReference, null)
                        .set(DeltaWorkerSettlementDO::getPayRemark, null)
        );
        if (rows != 1) {
            throw exception(SETTLEMENT_STATUS_CHANGED);
        }

        writeSettlementLog(settlementId, settlement.getServiceOrderId(),
                SettlementOperationTypeEnum.REVOKE_PAID.getType(),
                oldStatus, SettlementStatusEnum.APPROVED.getStatus(),
                OperatorTypeEnum.ADMIN.getType(), adminUserId,
                "管理员ID=" + adminUserId + " 撤销打款标记，原因：" + reason);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 核心创建结算逻辑
     */
    private DeltaWorkerSettlementDO doCreateSettlement(Long serviceOrderId, Integer operatorType, Long operatorId) {
        // 1. 查询服务单
        DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(serviceOrderId);
        if (order == null) {
            throw exception(SERVICE_ORDER_NOT_EXISTS);
        }

        // 2. 服务单必须已完成
        if (!cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum.isCompleted(order.getStatus())) {
            throw exception(SETTLEMENT_SERVICE_ORDER_NOT_COMPLETED);
        }

        // 3. 必须有打手
        Long workerId = order.getAssignedWorkerId();
        if (workerId == null) {
            throw exception(SETTLEMENT_NO_VALID_WORKER);
        }

        // 4. 幂等校验：已存在则抛异常
        DeltaWorkerSettlementDO existing = deltaWorkerSettlementService.getSettlementByServiceOrderId(serviceOrderId);
        if (existing != null) {
            throw exception(SETTLEMENT_ALREADY_EXISTS);
        }

        // 5. 获取金额和抽成比例
        int serviceAmount = (order.getServiceAmount() != null) ? order.getServiceAmount() : 0;
        if (serviceAmount <= 0) {
            throw exception(SETTLEMENT_AMOUNT_CALCULATE_FAILED);
        }

        // 抽成比例：优先从服务单快照读取
        Integer commissionRate = order.getCommissionRate();
        if (commissionRate == null || commissionRate <= 0) {
            // 回退到打手表
            DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
            if (worker != null && worker.getCommissionRate() != null && worker.getCommissionRate() > 0) {
                commissionRate = worker.getCommissionRate();
            }
        }
        if (commissionRate == null || commissionRate <= 0) {
            throw exception(SETTLEMENT_COMMISSION_RATE_NOT_CONFIGURED);
        }

        // 6. 计算金额（万分制，四舍五入到分）
        long platformFeeL = Math.round((long) serviceAmount * commissionRate / 10000.0);
        int platformFee = (int) platformFeeL;
        int workerAmount = serviceAmount - platformFee;

        // 7. 生成结算单号
        String settlementNo = deltaNoRedisDAO.generateSettlementNo();

        // 8. 创建结算记录
        LocalDateTime now = LocalDateTime.now();
        DeltaWorkerSettlementDO settlement = DeltaWorkerSettlementDO.builder()
                .settlementNo(settlementNo)
                .serviceOrderId(serviceOrderId)
                .workerId(workerId)
                .serviceAmount(serviceAmount)
                .commissionRate(commissionRate)
                .platformFee(platformFee)
                .workerAmount(workerAmount)
                .settlementStatus(SettlementStatusEnum.PENDING_REVIEW.getStatus())
                .settledAt(now)
                .build();

        try {
            deltaWorkerSettlementService.createSettlement(settlement);
        } catch (DuplicateKeyException e) {
            log.warn("结算记录已存在（唯一键冲突）serviceOrderId={}", serviceOrderId);
            throw exception(SETTLEMENT_ALREADY_EXISTS);
        }

        // 9. 写结算日志
        String amountSnapshot = String.format("{\"serviceAmount\":%d,\"commissionRate\":%d,\"platformFee\":%d,\"workerAmount\":%d}",
                serviceAmount, commissionRate, platformFee, workerAmount);
        writeSettlementLog(settlement.getId(), serviceOrderId,
                SettlementOperationTypeEnum.GENERATE.getType(),
                null, SettlementStatusEnum.PENDING_REVIEW.getStatus(),
                operatorType, operatorId,
                "生成结算，金额=" + serviceAmount + "分，抽成比例=" + commissionRate
                        + "（万分制），平台抽成=" + platformFee + "分，打手收入=" + workerAmount + "分",
                amountSnapshot);

        log.info("结算生成成功 settlementId={}, serviceOrderId={}, workerId={}, amount={}", 
                settlement.getId(), serviceOrderId, workerId, workerAmount);

        return settlement;
    }

    /**
     * 写结算操作日志
     */
    private void writeSettlementLog(Long settlementId, Long serviceOrderId, String operationType,
                                     Integer beforeStatus, Integer afterStatus,
                                     Integer operatorType, Long operatorId, String content) {
        writeSettlementLog(settlementId, serviceOrderId, operationType, beforeStatus, afterStatus,
                operatorType, operatorId, content, null);
    }

    private void writeSettlementLog(Long settlementId, Long serviceOrderId, String operationType,
                                     Integer beforeStatus, Integer afterStatus,
                                     Integer operatorType, Long operatorId, String content, String amountSnapshot) {
        DeltaWorkerSettlementLogDO logEntry = DeltaWorkerSettlementLogDO.builder()
                .settlementId(settlementId)
                .serviceOrderId(serviceOrderId)
                .operationType(operationType)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .content(content)
                .amountSnapshot(amountSnapshot)
                .build();
        deltaWorkerSettlementLogService.createLog(logEntry);
    }

    // ========== Phase 10 事件发布辅助方法 ==========

    private void publishSettlementEvent(DeltaWorkerSettlementDO settlement,
                                         cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum eventType,
                                         cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum template) {
        try {
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(settlement.getServiceOrderId());
            Long workerUserId = getWorkerUserId(settlement.getWorkerId());
            if (workerUserId == null || order == null) return;
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("settlementNo", settlement.getSettlementNo());
            params.put("orderNo", order.getServiceOrderNo());
            params.put("amount", String.valueOf(settlement.getWorkerAmount()));
            deltaEventPublisher.publishToWorker(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(eventType.getType())
                    .tenantId(settlement.getTenantId())
                    .aggregateType("SETTLEMENT")
                    .aggregateId(settlement.getId())
                    .bizKey(eventType.getType() + ":" + settlement.getId() + ":" + workerUserId)
                    .recipientId(workerUserId)
                    .templateCode(template.getCode())
                    .templateParams(params)
                    .build());
        } catch (Exception e) {
            log.error("结算事件写入Outbox失败 settlementId={}, eventType={}", settlement.getId(), eventType, e);
        }
    }

    private void publishSettlementEventWithReason(DeltaWorkerSettlementDO settlement,
                                                   cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum eventType,
                                                   cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum template,
                                                   String reason) {
        try {
            DeltaServiceOrderDO order = deltaServiceOrderMapper.selectById(settlement.getServiceOrderId());
            Long workerUserId = getWorkerUserId(settlement.getWorkerId());
            if (workerUserId == null || order == null) return;
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("settlementNo", settlement.getSettlementNo());
            params.put("orderNo", order.getServiceOrderNo());
            params.put("reason", reason != null ? reason : "");
            deltaEventPublisher.publishToWorker(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(eventType.getType())
                    .tenantId(settlement.getTenantId())
                    .aggregateType("SETTLEMENT")
                    .aggregateId(settlement.getId())
                    .bizKey(eventType.getType() + ":" + settlement.getId() + ":" + workerUserId)
                    .recipientId(workerUserId)
                    .templateCode(template.getCode())
                    .templateParams(params)
                    .build());
        } catch (Exception e) {
            log.error("结算事件写入Outbox失败 settlementId={}, eventType={}", settlement.getId(), eventType, e);
        }
    }

    private void publishSettlementEventWithAmount(DeltaWorkerSettlementDO settlement,
                                                   cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum eventType,
                                                   cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum template) {
        try {
            Long workerUserId = getWorkerUserId(settlement.getWorkerId());
            if (workerUserId == null) return;
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("settlementNo", settlement.getSettlementNo());
            params.put("amount", String.valueOf(settlement.getWorkerAmount()));
            deltaEventPublisher.publishToWorker(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                    .eventType(eventType.getType())
                    .tenantId(settlement.getTenantId())
                    .aggregateType("SETTLEMENT")
                    .aggregateId(settlement.getId())
                    .bizKey(eventType.getType() + ":" + settlement.getId() + ":" + workerUserId)
                    .recipientId(workerUserId)
                    .templateCode(template.getCode())
                    .templateParams(params)
                    .build());
        } catch (Exception e) {
            log.error("结算事件写入Outbox失败 settlementId={}, eventType={}", settlement.getId(), eventType, e);
        }
    }

    private Long getWorkerUserId(Long workerId) {
        if (workerId == null) return null;
        DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
        return worker != null ? worker.getUserId() : null;
    }

}
