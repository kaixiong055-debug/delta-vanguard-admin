package cn.iocoder.yudao.module.delta.service.event;

import cn.iocoder.yudao.module.delta.config.DeltaReminderProperties;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaReminderRecordDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaReminderRecordMapper;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundRecordMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum;
import cn.iocoder.yudao.module.delta.enums.event.NotificationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.event.RecipientTypeEnum;
import cn.iocoder.yudao.module.delta.enums.event.ReminderTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.RefundStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.RecoveryStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delta 超时提醒服务
 * <p>
 * 只负责提醒，不改变任何业务状态。
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaReminderService {

    @Resource
    private DeltaReminderProperties deltaReminderProperties;
    @Resource
    private DeltaReminderRecordMapper deltaReminderRecordMapper;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;
    @Resource
    private DeltaMemberNotificationService deltaMemberNotificationService;
    @Resource
    private DeltaWorkerMapper deltaWorkerMapper;

    /**
     * 扫描并发送超时提醒
     */
    public void scanAndRemind() {
        // 扫描每种提醒类型的到期业务
        scanDispatchPending();
        scanWorkerStartPending();
        scanProgressSilent();
        scanAcceptancePending();
        scanRefundPending();
        scanRecoveryPending();
    }

    // ========== 待派单超时 ==========

    private void scanDispatchPending() {
        int minutes = deltaReminderProperties.getDispatchPendingMinutes();
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .eq(DeltaServiceOrderDO::getStatus, ServiceOrderStatusEnum.POOL_PENDING.getStatus())
                        .le(DeltaServiceOrderDO::getCreateTime, threshold)
                        .last("LIMIT 50"));

        for (DeltaServiceOrderDO order : orders) {
            trySendReminder(
                    ReminderTypeEnum.DISPATCH_PENDING,
                    "SERVICE_ORDER", order.getId(),
                    null, RecipientTypeEnum.ADMIN.getType(),
                    DeltaNotificationTemplateEnum.REMINDER_DISPATCH_PENDING,
                    buildHoursParam(minutes, "orderNo", order.getServiceOrderNo()),
                    order.getTenantId()
            );
        }
    }

    // ========== 打手待开始超时 ==========

    private void scanWorkerStartPending() {
        int minutes = deltaReminderProperties.getWorkerStartMinutes();
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .eq(DeltaServiceOrderDO::getStatus, ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus())
                        .le(DeltaServiceOrderDO::getAcceptedAt, threshold)
                        .isNotNull(DeltaServiceOrderDO::getAcceptedAt)
                        .last("LIMIT 50"));

        for (DeltaServiceOrderDO order : orders) {
            Long workerUserId = getWorkerUserId(order);
            if (workerUserId != null) {
                trySendReminder(
                        ReminderTypeEnum.WORKER_START_PENDING,
                        "SERVICE_ORDER", order.getId(),
                        workerUserId, RecipientTypeEnum.WORKER.getType(),
                        DeltaNotificationTemplateEnum.REMINDER_WORKER_START,
                        buildHoursParam(minutes, "orderNo", order.getServiceOrderNo()),
                        order.getTenantId()
                );
            }
            // 同步通知后台
            trySendReminder(
                    ReminderTypeEnum.WORKER_START_PENDING,
                    "SERVICE_ORDER", order.getId(),
                    null, RecipientTypeEnum.ADMIN.getType(),
                    DeltaNotificationTemplateEnum.REMINDER_WORKER_START_ADMIN,
                    buildHoursParam(minutes, "orderNo", order.getServiceOrderNo()),
                    order.getTenantId()
            );
        }
    }

    // ========== 服务无进度超时 ==========

    private void scanProgressSilent() {
        int hours = deltaReminderProperties.getProgressSilentHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        // 查询 IN_PROGRESS 且最后进度时间超过阈值的订单
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .eq(DeltaServiceOrderDO::getStatus, ServiceOrderStatusEnum.IN_PROGRESS.getStatus())
                        .and(w -> w.isNull(DeltaServiceOrderDO::getUpdateTime)
                                .or().le(DeltaServiceOrderDO::getUpdateTime, threshold))
                        .last("LIMIT 50"));

        for (DeltaServiceOrderDO order : orders) {
            Long workerUserId = getWorkerUserId(order);
            if (workerUserId != null) {
                trySendReminder(
                        ReminderTypeEnum.PROGRESS_SILENT,
                        "SERVICE_ORDER", order.getId(),
                        workerUserId, RecipientTypeEnum.WORKER.getType(),
                        DeltaNotificationTemplateEnum.REMINDER_PROGRESS_SILENT,
                        buildHoursParam(hours, "orderNo", order.getServiceOrderNo()),
                        order.getTenantId()
                );
            }
        }
    }

    // ========== 待验收超时 ==========

    private void scanAcceptancePending() {
        int hours = deltaReminderProperties.getAcceptancePendingHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .eq(DeltaServiceOrderDO::getStatus, ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus())
                        .le(DeltaServiceOrderDO::getSubmittedAt, threshold)
                        .isNotNull(DeltaServiceOrderDO::getSubmittedAt)
                        .last("LIMIT 50"));

        for (DeltaServiceOrderDO order : orders) {
            trySendReminder(
                    ReminderTypeEnum.ACCEPTANCE_PENDING,
                    "SERVICE_ORDER", order.getId(),
                    order.getBuyerUserId(), RecipientTypeEnum.BUYER.getType(),
                    DeltaNotificationTemplateEnum.REMINDER_ACCEPTANCE_PENDING,
                    buildHoursParam(hours, "orderNo", order.getServiceOrderNo()),
                    order.getTenantId()
            );
        }
    }

    // ========== 退款待处理超时 ==========

    private void scanRefundPending() {
        int hours = deltaReminderProperties.getRefundPendingHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .eq(DeltaRefundRecordDO::getRefundStatus, RefundStatusEnum.PENDING_MANUAL.getStatus())
                        .le(DeltaRefundRecordDO::getCreateTime, threshold)
                        .last("LIMIT 50"));

        for (DeltaRefundRecordDO refund : refunds) {
            trySendReminder(
                    ReminderTypeEnum.REFUND_PENDING,
                    "REFUND", refund.getId(),
                    null, RecipientTypeEnum.ADMIN.getType(),
                    DeltaNotificationTemplateEnum.REMINDER_REFUND_PENDING,
                    buildParamMap("refundNo", refund.getRefundNo(), "hours", String.valueOf(hours)),
                    refund.getTenantId()
            );
        }
    }

    // ========== 追回待处理超时 ==========

    private void scanRecoveryPending() {
        int hours = deltaReminderProperties.getRecoveryPendingHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .and(w -> w.eq(DeltaFundRecoveryDO::getRecoveryStatus, RecoveryStatusEnum.PENDING.getStatus())
                                .or().eq(DeltaFundRecoveryDO::getRecoveryStatus, RecoveryStatusEnum.PROCESSING.getStatus()))
                        .le(DeltaFundRecoveryDO::getCreateTime, threshold)
                        .last("LIMIT 50"));

        for (DeltaFundRecoveryDO recovery : recoveries) {
            trySendReminder(
                    ReminderTypeEnum.RECOVERY_PENDING,
                    "FUND_RECOVERY", recovery.getId(),
                    null, RecipientTypeEnum.ADMIN.getType(),
                    DeltaNotificationTemplateEnum.REMINDER_RECOVERY_PENDING,
                    buildParamMap("recoveryNo", recovery.getRecoveryNo(), "hours", String.valueOf(hours)),
                    recovery.getTenantId()
            );
        }
    }

    // ========== 通用提醒发送 ==========

    @Transactional(rollbackFor = Exception.class)
    public void trySendReminder(ReminderTypeEnum reminderType, String bizType, Long bizId,
                                 Long recipientId, String recipientType,
                                 DeltaNotificationTemplateEnum template,
                                 Map<String, String> params, Long tenantId) {
        // 1. 检查冷却
        DeltaReminderRecordDO record = deltaReminderRecordMapper.selectForCooldown(
                reminderType.getType(), bizType, bizId, recipientId, tenantId);
        if (record != null) {
            int cooldownHours = deltaReminderProperties.getCooldownHours();
            if (record.getLastRemindTime() != null
                    && record.getLastRemindTime().plusHours(cooldownHours).isAfter(LocalDateTime.now())) {
                log.debug("提醒冷却中，跳过 reminderType={}, bizId={}, recipientId={}", reminderType.getType(), bizId, recipientId);
                return;
            }
            // 更新提醒记录
            deltaReminderRecordMapper.updateReminderTime(record.getId(), LocalDateTime.now());
        } else {
            // 创建提醒记录
            DeltaReminderRecordDO newRecord = DeltaReminderRecordDO.builder()
                    .reminderType(reminderType.getType())
                    .bizType(bizType)
                    .bizId(bizId)
                    .recipientId(recipientId)
                    .recipientType(recipientType)
                    .lastRemindTime(LocalDateTime.now())
                    .remindCount(1)
                    .tenantId(tenantId)
                    .build();
            deltaReminderRecordMapper.insert(newRecord);
        }

        // 2. 创建站内通知
        if (recipientId == null) {
            // 管理员通知：创建系统级通知（无具体接收人）
            log.info("管理员提醒，等待后台消费 reminderType={}, bizId={}", reminderType.getType(), bizId);
            return;
        }

        String userType = RecipientTypeEnum.BUYER.getType().equals(recipientType) ? "BUYER" : "WORKER";
        String title = template.getTitle();
        String content = template.render(params);

        deltaMemberNotificationService.createNotification(
                recipientId, userType,
                NotificationTypeEnum.REMINDER.getType(),
                title, content, bizType, bizId,
                null, // 提醒不和outbox事件绑定
                tenantId
        );

        log.info("超时提醒已发送 reminderType={}, bizId={}, recipientId={}", reminderType.getType(), bizId, recipientId);
    }

    // ========== 辅助方法 ==========

    private Long getWorkerUserId(DeltaServiceOrderDO order) {
        if (order.getAssignedWorkerId() == null) return null;
        DeltaWorkerDO worker = deltaWorkerMapper.selectById(order.getAssignedWorkerId());
        return worker != null ? worker.getUserId() : null;
    }

    private Map<String, String> buildHoursParam(int totalMinutes, String keyName, String value) {
        double hours = Math.max(0.1, totalMinutes / 60.0);
        String hoursStr = String.format("%.1f", hours);
        Map<String, String> params = new HashMap<>();
        params.put(keyName, value);
        params.put("hours", hoursStr);
        return params;
    }

    private Map<String, String> buildParamMap(String k1, String v1, String k2, String v2) {
        Map<String, String> params = new HashMap<>();
        params.put(k1, v1);
        params.put(k2, v2);
        return params;
    }
}
