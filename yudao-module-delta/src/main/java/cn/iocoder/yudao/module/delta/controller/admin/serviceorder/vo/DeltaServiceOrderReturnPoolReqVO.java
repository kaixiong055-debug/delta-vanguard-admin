package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Admin - 服务单退回订单池 Req VO
 */
@Schema(description = "管理后台 - 服务单退回订单池 Request VO")
@Data
public class DeltaServiceOrderReturnPoolReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "退回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "当前打手无法履约，退回订单池")
    @NotBlank(message = "退回原因不能为空")
    private String reason;

}
