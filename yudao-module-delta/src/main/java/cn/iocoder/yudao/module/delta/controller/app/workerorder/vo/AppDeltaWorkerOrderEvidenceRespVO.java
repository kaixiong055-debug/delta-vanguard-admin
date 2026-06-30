package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * App - 服务凭证 Response VO
 */
@Schema(description = "用户 App - 服务凭证 Response VO")
@Data
public class AppDeltaWorkerOrderEvidenceRespVO {

    @Schema(description = "凭证ID", example = "1")
    private Long id;

    @Schema(description = "服务单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "打手ID", example = "100")
    private Long workerId;

    @Schema(description = "凭证类型：1-截图 2-视频 3-文字说明 4-文件", example = "1")
    private Integer evidenceType;

    @Schema(description = "凭证类型名称", example = "截图")
    private String evidenceTypeName;

    @Schema(description = "凭证内容/说明", example = "任务完成截图")
    private String content;

    @Schema(description = "凭证图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "凭证视频URL")
    private String videoUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
