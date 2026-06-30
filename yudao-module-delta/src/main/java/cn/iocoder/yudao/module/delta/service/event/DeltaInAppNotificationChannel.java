package cn.iocoder.yudao.module.delta.service.event;

import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaMemberNotificationDO;
import cn.iocoder.yudao.module.delta.enums.event.NotificationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.event.RecipientTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * IN_APP 站内通知渠道实现
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaInAppNotificationChannel implements DeltaNotificationChannel {

    @Resource
    private DeltaMemberNotificationService deltaMemberNotificationService;

    @Override
    public String getChannel() {
        return "IN_APP";
    }

    @Override
    public DeltaChannelSendResult send(DeltaChannelSendRequest request) {
        try {
            String userType = RecipientTypeEnum.BUYER.getType().equals(request.getRecipientType())
                    ? "BUYER" : "WORKER";

            deltaMemberNotificationService.createNotification(
                    request.getRecipientId(),
                    userType,
                    NotificationTypeEnum.ORDER.getType(),
                    request.getTitle(),
                    request.getContent(),
                    request.getAggregateType(),
                    request.getAggregateId(),
                    request.getEventOutboxId(),
                    request.getTenantId()
            );

            log.debug("站内通知已发送 eventOutboxId={}, userId={}", request.getEventOutboxId(), request.getRecipientId());
            return DeltaChannelSendResult.success("OK", "站内通知发送成功");
        } catch (Exception e) {
            log.error("站内通知发送失败 eventOutboxId={}, error={}", request.getEventOutboxId(), e.getMessage());
            return DeltaChannelSendResult.failed(e.getMessage(), true);
        }
    }
}
