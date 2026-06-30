package cn.iocoder.yudao.module.delta.controller.app.orderpool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * App - 打手接单 Req VO
 */
@Schema(description = "用户 App - 打手接单 Request VO")
@Data
public class AppDeltaOrderPoolClaimReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

}
