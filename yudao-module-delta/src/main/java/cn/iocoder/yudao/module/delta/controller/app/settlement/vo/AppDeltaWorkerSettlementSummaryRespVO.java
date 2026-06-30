package cn.iocoder.yudao.module.delta.controller.app.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - 打手结算汇总响应 VO
 */
@Schema(description = "用户 App - 打手结算汇总 Response VO")
@Data
public class AppDeltaWorkerSettlementSummaryRespVO {

    @Schema(description = "待审核金额（分）", example = "8500")
    private Long pendingReviewAmount;

    @Schema(description = "审核通过金额（分）", example = "17000")
    private Long approvedAmount;

    @Schema(description = "已打款金额（分）", example = "8500")
    private Long paidAmount;

    @Schema(description = "审核驳回金额（分）", example = "0")
    private Long rejectedAmount;

    @Schema(description = "累计已打款金额（分）", example = "8500")
    private Long totalPaidAmount;

}
