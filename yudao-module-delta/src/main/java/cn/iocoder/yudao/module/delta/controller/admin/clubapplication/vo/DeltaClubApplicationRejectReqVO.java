package cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 俱乐部入驻申请审核拒绝 Request VO")
@Data
public class DeltaClubApplicationRejectReqVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "资质不全")
    @NotBlank(message = "拒绝原因不能为空")
    @Length(max = 500, message = "拒绝原因最大 500 字")
    private String reason;

}
