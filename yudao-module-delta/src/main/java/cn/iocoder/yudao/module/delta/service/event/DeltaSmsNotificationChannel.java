package cn.iocoder.yudao.module.delta.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信通知渠道 —— 预留空实现，Phase 11 不接入短信
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaSmsNotificationChannel implements DeltaNotificationChannel {

    @Override
    public String getChannel() {
        return "SMS";
    }

    @Override
    public DeltaChannelSendResult send(DeltaChannelSendRequest request) {
        log.info("短信渠道尚未启用 eventOutboxId={}", request.getEventOutboxId());
        return DeltaChannelSendResult.skipped("渠道尚未启用");
    }
}
