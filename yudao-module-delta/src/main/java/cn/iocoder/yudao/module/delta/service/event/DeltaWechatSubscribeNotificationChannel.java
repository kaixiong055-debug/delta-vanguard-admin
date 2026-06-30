package cn.iocoder.yudao.module.delta.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 微信订阅消息渠道 —— 预留空实现，Phase 11 不接入真实微信
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaWechatSubscribeNotificationChannel implements DeltaNotificationChannel {

    @Override
    public String getChannel() {
        return "WECHAT_SUBSCRIBE";
    }

    @Override
    public DeltaChannelSendResult send(DeltaChannelSendRequest request) {
        log.info("微信订阅消息渠道尚未启用 eventOutboxId={} eventType={}", request.getEventOutboxId(), request.getEventType());
        return DeltaChannelSendResult.skipped("渠道尚未启用");
    }
}
