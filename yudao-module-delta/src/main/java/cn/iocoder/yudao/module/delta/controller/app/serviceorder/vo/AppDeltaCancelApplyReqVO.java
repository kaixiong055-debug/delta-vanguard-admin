package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * App - 提交取消申请 Request VO
 */
@Schema(description = "App - 提交取消申请 Request VO")
@Data
public class AppDeltaCancelApplyReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "取消原因", example = "暂时不需要该服务")
    private String reason;

    @Schema(description = "备注", example = "请协助取消")
    private String remark;

}
