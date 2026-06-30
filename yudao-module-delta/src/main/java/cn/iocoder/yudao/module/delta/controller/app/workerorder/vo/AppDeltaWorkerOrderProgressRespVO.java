package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App - 服务进度 Response VO
 */
@Schema(description = "用户 App - 服务进度 Response VO")
@Data
public class AppDeltaWorkerOrderProgressRespVO {

    @Schema(description = "进度ID", example = "1")
    private Long id;

    @Schema(description = "服务单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "打手ID", example = "100")
    private Long workerId;

    @Schema(description = "进度类型：1-开始服务 2-进度更新 3-异常报告 4-提交完成", example = "2")
    private Integer progressType;

    @Schema(description = "进度类型名称", example = "进度更新")
    private String progressTypeName;

    @Schema(description = "进度百分比", example = "30")
    private Integer progressPercent;

    @Schema(description = "进度内容", example = "已完成第一阶段任务")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
