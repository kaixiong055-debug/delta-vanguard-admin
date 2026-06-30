package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 服务单确认 Req VO
 */
@Schema(description = "管理后台 - 服务单确认 Request VO")
@Data
public class DeltaServiceOrderConfirmReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long id;

    @Schema(description = "确认备注", example = "已和客户确认服务要求")
    private String remark;

}
