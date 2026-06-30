package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Admin - 驳回售后 Request VO
 */
@Schema(description = "管理后台 - 驳回售后 Request VO")
@Data
public class DeltaAfterSaleRejectReqVO {

    @Schema(description = "售后案件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "售后案件ID不能为空")
    private Long id;

    @Schema(description = "驳回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "提交的凭证不足")
    @NotBlank(message = "驳回原因不能为空")
    private String reason;

}
