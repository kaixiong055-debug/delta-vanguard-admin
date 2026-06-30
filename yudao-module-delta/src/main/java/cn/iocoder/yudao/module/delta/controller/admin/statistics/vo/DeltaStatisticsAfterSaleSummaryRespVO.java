package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 售后与资金统计响应 VO
 */
@Schema(description = "管理后台 - 售后与资金统计")
@Data
public class DeltaStatisticsAfterSaleSummaryRespVO {

    // ====== 售后 ======

    @Schema(description = "售后案件总数")
    private Long afterSaleCount;

    @Schema(description = "待处理售后")
    private Long pendingAfterSaleCount;

    @Schema(description = "已仲裁")
    private Long arbitratedAfterSaleCount;

    @Schema(description = "已关闭")
    private Long closedAfterSaleCount;

    // ====== 退款 ======

    @Schema(description = "退款笔数")
    private Long refundCount;

    @Schema(description = "待退款")
    private Long pendingRefundCount;

    @Schema(description = "已完成退款")
    private Long completedRefundCount;

    @Schema(description = "退款总金额（分）")
    private Long refundAmount;

    // ====== 追回 ======

    @Schema(description = "追回任务数")
    private Long recoveryTaskCount;

    @Schema(description = "处理中追回")
    private Long processingRecoveryCount;

    @Schema(description = "已完成追回")
    private Long completedRecoveryCount;

    @Schema(description = "应追回金额（分）")
    private Long shouldRecoverAmount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

    @Schema(description = "待追回金额（分）")
    private Long remainingRecoveryAmount;

}
