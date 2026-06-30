package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 批准取消 Request VO
 */
@Schema(description = "管理后台 - 批准取消 Request VO")
@Data
public class DeltaOrderCancelApproveReqVO {

    @Schema(description = "取消申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "取消申请ID不能为空")
    private Long id;

    @Schema(description = "退款金额（分）", example = "5000")
    private Integer refundAmount;

    @Schema(description = "责任归属：0-买家 1-打手 2-平台 3-共同 4-无", example = "0")
    private Integer responsibilityType;

    @Schema(description = "备注", example = "同意取消")
    private String remark;

}
