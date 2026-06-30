package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Admin - 服务单改派 Req VO
 */
@Schema(description = "管理后台 - 服务单改派 Request VO")
@Data
public class DeltaServiceOrderReassignReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "新打手ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "新打手ID不能为空")
    private Long newWorkerId;

    @Schema(description = "改派原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "原打手临时无法履约")
    @NotBlank(message = "改派原因不能为空")
    private String reason;

}
