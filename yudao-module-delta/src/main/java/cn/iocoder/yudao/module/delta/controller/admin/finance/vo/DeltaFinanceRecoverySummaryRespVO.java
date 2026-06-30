package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 追回汇总响应 VO
 */
@Schema(description = "管理后台 - 追回汇总")
@Data
public class DeltaFinanceRecoverySummaryRespVO {

    @Schema(description = "追回任务总数")
    private Long totalCount;

    @Schema(description = "待处理")
    private Long pendingCount;

    @Schema(description = "处理中")
    private Long processingCount;

    @Schema(description = "部分追回")
    private Long partiallyRecoveredCount;

    @Schema(description = "已全部追回")
    private Long recoveredCount;

    @Schema(description = "失败")
    private Long failedCount;

    @Schema(description = "取消")
    private Long canceledCount;

    @Schema(description = "应追回金额（分）")
    private Long shouldRecoverAmount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

    @Schema(description = "待追回金额（分）")
    private Long remainingAmount;

}
