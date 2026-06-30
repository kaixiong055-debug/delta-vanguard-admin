package cn.iocoder.yudao.module.delta.service.event;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaEventOutboxMapper;
import cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum;
import cn.iocoder.yudao.module.delta.enums.event.EventOutboxStatusEnum;
import cn.iocoder.yudao.module.delta.enums.event.RecipientTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Outbox 事件消费服务
 * <p>
 * 负责从 Outbox 读取事件 -> 构造通知 -> 写入站内消息 -> 标记成功
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaEventOutboxConsumeService {

    /** 最大重试次数 */
    public static final int MAX_RETRY_COUNT = 5;

    /** 退避策略（分钟）：1, 5, 15, 60, 360 */
    private static final long[] RETRY_BACKOFF_MINUTES = {1, 5, 15, 60, 360};

    /** 错误摘要最大长度 */
    private static final int MAX_LAST_ERROR_LENGTH = 500;

    @Resource
    private DeltaEventOutboxMapper deltaEventOutboxMapper;
    @Resource
    private DeltaMemberNotificationService deltaMemberNotificationService;

    /**
     * 处理单个事件
     *
     * @return 是否消费成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean processEvent(DeltaEventOutboxDO event) {
        Long eventId = event.getId();

        // 1. CAS: PENDING/FAILED -> PROCESSING
        int casResult = deltaEventOutboxMapper.casStartProcessing(eventId, event.getEventStatus());
        if (casResult != 1) {
            log.info("事件CAS失败，已被其他消费者处理 eventId={}", eventId);
            return false;
        }

        try {
            // 2. 解析模板
            if (event.getTemplateCode() == null) {
                // 无需通知的事件（如SYSTEM事件），直接标记成功
                deltaEventOutboxMapper.casMarkSuccess(eventId, LocalDateTime.now());
                log.info("事件无需通知，直接标记成功 eventId={}, eventType={}", eventId, event.getEventType());
                return true;
            }

            DeltaNotificationTemplateEnum template = DeltaNotificationTemplateEnum.fromCode(event.getTemplateCode());
            if (template == null) {
                throw exception(EVENT_TEMPLATE_NOT_FOUND);
            }

            // 3. 解析模板参数
            @SuppressWarnings("unchecked")
            Map<String, String> params = event.getTemplateParams() != null
                    ? JSONUtil.toBean(event.getTemplateParams(), Map.class)
                    : null;

            if (!template.validateParams(params)) {
                throw exception(EVENT_TEMPLATE_PARAMS_INCOMPLETE);
            }

            // 4. 校验接收人
            if (event.getRecipientId() == null) {
                throw exception(EVENT_RECIPIENT_NOT_FOUND);
            }

            String userType = mapRecipientToUserType(event.getRecipientType());

            // 5. 创建站内消息（同一事务）
            deltaMemberNotificationService.createNotification(
                    event.getRecipientId(),
                    userType,
                    template.getNotificationType().getType(),
                    template.getTitle(),
                    template.render(params),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getId(),
                    event.getTenantId()
            );

            // 6. 标记成功（同一事务）
            deltaEventOutboxMapper.casMarkSuccess(eventId, LocalDateTime.now());
            log.info("事件消费成功 eventId={}, eventType={}, recipientId={}", eventId, event.getEventType(), event.getRecipientId());
            return true;

        } catch (Exception e) {
            log.error("事件消费失败 eventId={}, eventType={}, error={}", eventId, event.getEventType(), e.getMessage());

            int newRetryCount = (event.getRetryCount() != null ? event.getRetryCount() : 0) + 1;
            String errorMsg = truncateError(e.getMessage());
            LocalDateTime now = LocalDateTime.now();

            if (newRetryCount >= MAX_RETRY_COUNT) {
                // 超过最大重试次数 -> DEAD
                deltaEventOutboxMapper.casMarkDead(eventId);
                log.warn("事件超过最大重试次数，标记DEAD eventId={}, retryCount={}", eventId, newRetryCount);
            } else {
                // 退避重试
                long backoffMinutes = RETRY_BACKOFF_MINUTES[Math.min(newRetryCount - 1, RETRY_BACKOFF_MINUTES.length - 1)];
                LocalDateTime nextRetryTime = now.plusMinutes(backoffMinutes);
                deltaEventOutboxMapper.casMarkFailed(eventId, newRetryCount, errorMsg, nextRetryTime);
                log.info("事件失败将重试 eventId={}, retryCount={}, nextRetryTime={}", eventId, newRetryCount, nextRetryTime);
            }
            return false;
        }
    }

    /**
     * 恢复长时间 PROCESSING 的事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void recoverStuckEvents(int limit) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);  // 30分钟未完成
        List<DeltaEventOutboxDO> stuckEvents = deltaEventOutboxMapper.selectStuckProcessingEvents(threshold, limit);
        for (DeltaEventOutboxDO event : stuckEvents) {
            int rows = deltaEventOutboxMapper.casRecoverStuckToFailed(event.getId(), "PROCESSING超时自动恢复");
            if (rows == 1) {
                log.info("恢复PROCESSING超时事件 eventId={}, eventType={}", event.getId(), event.getEventType());
            }
        }
    }

    /**
     * 重试计算
     */
    public static long calculateBackoffMinutes(int retryCount) {
        int idx = Math.min(retryCount, RETRY_BACKOFF_MINUTES.length - 1);
        return RETRY_BACKOFF_MINUTES[idx];
    }

    private String mapRecipientToUserType(String recipientType) {
        if (RecipientTypeEnum.BUYER.getType().equals(recipientType)) {
            return "BUYER";
        }
        if (RecipientTypeEnum.WORKER.getType().equals(recipientType)) {
            return "WORKER";
        }
        return "ADMIN";
    }

    private String truncateError(String error) {
        if (error == null) return "未知错误";
        if (error.length() <= MAX_LAST_ERROR_LENGTH) return error;
        return error.substring(0, MAX_LAST_ERROR_LENGTH - 3) + "...";
    }
}
