package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Admin - 后台要求返工 Request VO
 */
@Schema(description = "管理后台 - 后台要求返工 Request VO")
@Data
public class DeltaServiceOrderReworkReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "返工原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成凭证不完整，请重新提交")
    @NotBlank(message = "返工原因不能为空")
    private String reason;

}
