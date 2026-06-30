package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 退款记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaRefundRecordPageReqVO extends PageParam {

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "服务单号")
    private String serviceOrderNo;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "买家用户ID")
    private Long buyerUserId;

    @Schema(description = "退款状态")
    private Integer refundStatus;

    @Schema(description = "退款方式")
    private Integer refundMethod;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] completedTime;

}
