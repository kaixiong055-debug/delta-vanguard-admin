package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * App - 老板要求返工 Request VO
 */
@Schema(description = "用户 App - 老板要求返工 Request VO")
@Data
public class AppDeltaServiceOrderReworkReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "返工原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "任务要求未全部完成，请补充处理")
    @NotBlank(message = "返工原因不能为空")
    private String reason;

}
