package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Admin - 驳回取消 Request VO
 */
@Schema(description = "管理后台 - 驳回取消 Request VO")
@Data
public class DeltaOrderCancelRejectReqVO {

    @Schema(description = "取消申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "取消申请ID不能为空")
    private Long id;

    @Schema(description = "驳回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "服务已经开始，无法取消")
    @NotBlank(message = "驳回原因不能为空")
    private String reason;

}
