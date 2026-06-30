package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * App - 老板验收通过 Request VO
 */
@Schema(description = "用户 App - 老板验收通过 Request VO")
@Data
public class AppDeltaServiceOrderAcceptReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "验收备注", example = "服务已完成，确认验收")
    private String remark;

}
