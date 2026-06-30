package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 后台验收通过 Request VO
 */
@Schema(description = "管理后台 - 后台验收通过 Request VO")
@Data
public class DeltaServiceOrderAcceptReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "验收备注", example = "后台审核凭证通过")
    private String remark;

}
