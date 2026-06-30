package cn.iocoder.yudao.module.delta.controller.app.notification.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * App - 通知分页请求 VO
 */
@Schema(description = "用户 App - 通知分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppDeltaNotificationPageReqVO extends PageParam {

    @Schema(description = "已读状态：0未读 1已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "通知类型", example = "ORDER")
    private String notificationType;

    @Schema(description = "创建时间开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeEnd;
}
