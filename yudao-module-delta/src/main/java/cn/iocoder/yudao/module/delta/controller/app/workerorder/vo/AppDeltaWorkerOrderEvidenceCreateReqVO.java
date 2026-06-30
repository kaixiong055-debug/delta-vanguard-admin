package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * App - 登记服务凭证 Request VO
 */
@Schema(description = "用户 App - 登记服务凭证 Request VO")
@Data
public class AppDeltaWorkerOrderEvidenceCreateReqVO {

    @Schema(description = "服务单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "凭证类型：1-截图 2-视频 3-文字说明 4-文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "凭证类型不能为空")
    private Integer evidenceType;

    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/file.png")
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;

    @Schema(description = "凭证描述/说明", example = "任务完成截图")
    private String description;

}
