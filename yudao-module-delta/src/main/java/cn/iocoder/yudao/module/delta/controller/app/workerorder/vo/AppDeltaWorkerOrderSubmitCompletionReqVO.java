package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * App - 提交服务完成 Request VO
 */
@Schema(description = "用户 App - 提交服务完成 Request VO")
@Data
public class AppDeltaWorkerOrderSubmitCompletionReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "完成总结", requiredMode = Schema.RequiredMode.REQUIRED, example = "服务已经完成，请验收")
    @NotBlank(message = "完成总结不能为空")
    private String summary;

}
