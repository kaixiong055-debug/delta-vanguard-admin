package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 服务单派单 Req VO
 */
@Schema(description = "管理后台 - 服务单派单 Request VO")
@Data
public class DeltaServiceOrderDispatchReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "打手ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "打手ID不能为空")
    private Long workerId;

    @Schema(description = "派单备注", example = "客服人工派单")
    private String remark;

}
