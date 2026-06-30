package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 退款汇总响应 VO
 */
@Schema(description = "管理后台 - 退款汇总")
@Data
public class DeltaFinanceRefundSummaryRespVO {

    @Schema(description = "退款总笔数")
    private Long totalCount;

    @Schema(description = "待人工退款")
    private Long pendingManualCount;

    @Schema(description = "处理中")
    private Long processingCount;

    @Schema(description = "已完成")
    private Long completedCount;

    @Schema(description = "已取消")
    private Long canceledCount;

    @Schema(description = "失败")
    private Long failedCount;

    @Schema(description = "退款总金额（分）")
    private Long totalRefundAmount;

    @Schema(description = "已完成退款金额（分）")
    private Long completedRefundAmount;

    @Schema(description = "待退款金额（分）")
    private Long pendingRefundAmount;

}
