package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * Admin - 取消申请分页 Request VO
 */
@Schema(description = "管理后台 - 取消申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaOrderCancelPageReqVO extends PageParam {

    @Schema(description = "取消单号", example = "DCN202401010000001")
    private String cancelNo;

    @Schema(description = "服务订单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "买家用户ID", example = "1")
    private Long buyerUserId;

    @Schema(description = "取消状态：0-待审核 1-已通过 2-已驳回", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
