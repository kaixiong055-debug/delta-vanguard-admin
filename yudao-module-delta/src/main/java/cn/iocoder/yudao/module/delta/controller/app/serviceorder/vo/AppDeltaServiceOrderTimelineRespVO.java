package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App - 履约时间线节点 Response VO
 */
@Schema(description = "用户 App - 履约时间线节点 Response VO")
@Data
public class AppDeltaServiceOrderTimelineRespVO {

    @Schema(description = "节点类型：PROGRESS/LOG/EVIDENCE", example = "PROGRESS")
    private String nodeType;

    @Schema(description = "标题", example = "提交服务进度")
    private String title;

    @Schema(description = "内容", example = "已完成第一阶段任务")
    private String content;

    @Schema(description = "操作人类型：CUSTOMER/WORKER/ADMIN/SYSTEM", example = "WORKER")
    private String operatorType;

    @Schema(description = "操作人名称", example = "王牌打手")
    private String operatorName;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

}
