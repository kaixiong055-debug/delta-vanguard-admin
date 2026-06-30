package cn.iocoder.yudao.module.delta.controller.admin.club.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 俱乐部经营状态更新 Request VO")
@Data
public class DeltaClubUpdateStatusReqVO {

    @Schema(description = "俱乐部ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "俱乐部ID不能为空")
    private Long id;

    @Schema(description = "经营状态：0-停用 1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "经营状态不能为空")
    private Integer businessStatus;

    @Schema(description = "变更原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "违规经营")
    @NotBlank(message = "变更原因不能为空")
    @Length(max = 500, message = "原因最大 500 字")
    private String reason;

}
