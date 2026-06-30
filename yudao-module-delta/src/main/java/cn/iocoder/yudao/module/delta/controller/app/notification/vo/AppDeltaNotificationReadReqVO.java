package cn.iocoder.yudao.module.delta.controller.app.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * App - 标记已读请求 VO
 */
@Schema(description = "用户 App - 标记已读 Request VO")
@Data
public class AppDeltaNotificationReadReqVO {

    @Schema(description = "通知ID", required = true, example = "1")
    @NotNull(message = "通知ID不能为空")
    private Long id;
}
