package cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理后台 - 审核驳回 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 审核驳回 Request VO")
@Data
public class DeltaWorkerApplicationRejectReqVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    @Schema(description = "驳回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "证明材料不清晰")
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因长度不能超过500个字符")
    private String rejectReason;

}
