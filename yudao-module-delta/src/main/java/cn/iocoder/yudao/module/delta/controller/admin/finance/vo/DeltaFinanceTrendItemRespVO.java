package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 财务趋势单项响应 VO
 */
@Schema(description = "管理后台 - 财务趋势单项")
@Data
public class DeltaFinanceTrendItemRespVO {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "服务金额（分）")
    private Long serviceAmount;

    @Schema(description = "平台佣金（分）")
    private Long platformFeeAmount;

    @Schema(description = "打手收入（分）")
    private Long workerIncomeAmount;

    @Schema(description = "结算金额（分）")
    private Long settlementAmount;

    @Schema(description = "已打款金额（分）")
    private Long paidSettlementAmount;

    @Schema(description = "退款金额（分）")
    private Long refundAmount;

    @Schema(description = "追回金额（分）")
    private Long recoveredAmount;

}
