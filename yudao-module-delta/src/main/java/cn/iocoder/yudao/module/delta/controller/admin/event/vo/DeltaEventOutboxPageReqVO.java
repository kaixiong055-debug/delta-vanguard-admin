package cn.iocoder.yudao.module.delta.controller.admin.event.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * Admin - Outbox 事件分页请求 VO
 */
@Schema(description = "管理后台 - Outbox 事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaEventOutboxPageReqVO extends PageParam {

    @Schema(description = "事件类型", example = "SERVICE_ORDER_DISPATCHED")
    private String eventType;

    @Schema(description = "事件状态", example = "0")
    private Integer eventStatus;

    @Schema(description = "聚合类型", example = "SERVICE_ORDER")
    private String aggregateType;

    @Schema(description = "聚合根ID", example = "1")
    private Long aggregateId;

    @Schema(description = "接收人类型", example = "WORKER")
    private String recipientType;

    @Schema(description = "创建时间开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTimeEnd;
}
