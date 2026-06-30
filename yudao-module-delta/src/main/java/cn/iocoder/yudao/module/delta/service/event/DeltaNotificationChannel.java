package cn.iocoder.yudao.module.delta.service.event;

/**
 * 通知渠道接口
 * <p>
 * 各渠道（站内通知、微信订阅、短信等）实现此接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaNotificationChannel {

    /**
     * 获取渠道标识
     *
     * @return 渠道标识：IN_APP / WECHAT_SUBSCRIBE / SMS / EMAIL
     */
    String getChannel();

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送结果
     */
    DeltaChannelSendResult send(DeltaChannelSendRequest request);

}
