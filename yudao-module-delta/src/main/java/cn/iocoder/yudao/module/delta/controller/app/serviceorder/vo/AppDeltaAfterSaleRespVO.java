package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - 售后案件响应 VO
 */
@Schema(description = "App - 售后案件响应 VO")
@Data
public class AppDeltaAfterSaleRespVO {

    @Schema(description = "售后案件ID", example = "1")
    private Long id;

    @Schema(description = "售后单号", example = "DAS202401010000001")
    private String afterSaleNo;

    @Schema(description = "服务订单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "服务订单号", example = "DSO202401010000001")
    private String serviceOrderNo;

    @Schema(description = "售后类型", example = "1")
    private Integer afterSaleType;

    @Schema(description = "申请原因", example = "服务结果与要求不一致")
    private String reason;

    @Schema(description = "问题描述", example = "具体问题说明")
    private String description;

    @Schema(description = "请求退款金额（分）", example = "5000")
    private Integer requestedRefundAmount;

    @Schema(description = "批准退款金额（分）", example = "3000")
    private Integer approvedRefundAmount;

    @Schema(description = "售后状态：0-待处理 1-已受理 2-已驳回 3-已仲裁 4-已关闭", example = "0")
    private Integer status;

    @Schema(description = "状态名称", example = "待处理")
    private String statusName;

    @Schema(description = "仲裁结果", example = "全额退款")
    private String arbitrationResult;

    @Schema(description = "创建时间", example = "2024-01-01 00:00:00")
    private String createTime;

    @Schema(description = "处理时间", example = "2024-01-01 01:00:00")
    private String handleTime;

}
