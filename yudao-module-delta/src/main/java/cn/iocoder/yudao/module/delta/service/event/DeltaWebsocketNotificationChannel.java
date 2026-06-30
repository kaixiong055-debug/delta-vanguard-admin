package cn.iocoder.yudao.module.delta.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket 推送渠道 —— 预留空实现，Phase 11 不接入 WebSocket
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaWebsocketNotificationChannel implements DeltaNotificationChannel {

    @Override
    public String getChannel() {
        return "WEBSOCKET";
    }

    @Override
    public DeltaChannelSendResult send(DeltaChannelSendRequest request) {
        log.info("WebSocket 渠道尚未启用 eventOutboxId={}", request.getEventOutboxId());
        return DeltaChannelSendResult.skipped("渠道尚未启用");
    }
}
