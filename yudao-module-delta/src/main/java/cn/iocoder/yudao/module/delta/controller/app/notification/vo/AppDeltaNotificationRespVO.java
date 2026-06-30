package cn.iocoder.yudao.module.delta.controller.app.notification.vo;

import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaMemberNotificationDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App - 通知响应 VO
 */
@Schema(description = "用户 App - 通知 Response VO")
@Data
public class AppDeltaNotificationRespVO {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "通知类型", example = "ORDER")
    private String notificationType;

    @Schema(description = "通知标题", example = "订单已派给你")
    private String title;

    @Schema(description = "通知内容", example = "订单 DSO20260101001 已指派给你，请尽快处理")
    private String content;

    @Schema(description = "业务类型", example = "SERVICE_ORDER")
    private String bizType;

    @Schema(description = "业务ID", example = "1")
    private Long bizId;

    @Schema(description = "是否已读: false未读 true已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "已读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static AppDeltaNotificationRespVO from(DeltaMemberNotificationDO notification) {
        AppDeltaNotificationRespVO vo = new AppDeltaNotificationRespVO();
        vo.setId(notification.getId());
        vo.setNotificationType(notification.getNotificationType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setBizType(notification.getBizType());
        vo.setBizId(notification.getBizId());
        vo.setReadStatus(notification.getReadStatus());
        vo.setReadTime(notification.getReadTime());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}
