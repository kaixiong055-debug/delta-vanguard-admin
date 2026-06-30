package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 财务总览响应 VO
 */
@Schema(description = "管理后台 - 财务总览")
@Data
public class DeltaFinanceSummaryRespVO {

    // ====== 收入 ======

    @Schema(description = "服务订单金额（分）")
    private Long serviceOrderAmount;

    @Schema(description = "平台佣金收入（分）")
    private Long platformFeeAmount;

    @Schema(description = "打手收入（分）")
    private Long workerIncomeAmount;

    // ====== 结算 ======

    @Schema(description = "结算总金额（分）")
    private Long settlementAmount;

    @Schema(description = "审核通过结算金额（分）")
    private Long approvedSettlementAmount;

    @Schema(description = "已打款金额（分）")
    private Long paidSettlementAmount;

    @Schema(description = "待结算金额（分）")
    private Long pendingSettlementAmount;

    // ====== 退款 ======

    @Schema(description = "退款总金额（分）")
    private Long refundAmount;

    @Schema(description = "已完成退款金额（分）")
    private Long completedRefundAmount;

    @Schema(description = "待退款金额（分）")
    private Long pendingRefundAmount;

    // ====== 追回 ======

    @Schema(description = "应追回金额（分）")
    private Long shouldRecoverAmount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

    @Schema(description = "待追回金额（分）")
    private Long remainingRecoveryAmount;

}
