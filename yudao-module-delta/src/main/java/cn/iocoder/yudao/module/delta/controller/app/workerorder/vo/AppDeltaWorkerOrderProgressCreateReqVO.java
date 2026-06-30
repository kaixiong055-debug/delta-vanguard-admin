package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * App - 提交服务进度 Request VO
 */
@Schema(description = "用户 App - 提交服务进度 Request VO")
@Data
public class AppDeltaWorkerOrderProgressCreateReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "进度类型：1-开始服务 2-进度更新 3-异常报告 4-提交完成", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "进度类型不能为空")
    private Integer progressType;

    @Schema(description = "进度内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "已完成第一阶段任务")
    @NotBlank(message = "进度内容不能为空")
    private String content;

    @Schema(description = "进度百分比(0-100)", example = "30")
    @Min(value = 0, message = "进度百分比不能小于0")
    @Max(value = 100, message = "进度百分比不能大于100")
    private Integer progressPercent;

}
