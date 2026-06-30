package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运营总览响应 VO
 */
@Schema(description = "管理后台 - 运营总览")
@Data
public class DeltaStatisticsOverviewRespVO {

    // ====== 订单统计 ======

    @Schema(description = "订单总量")
    private Long totalOrderCount;

    @Schema(description = "待派单")
    private Long pendingClaimCount;

    @Schema(description = "履约中订单量")
    private Long processingOrderCount;

    @Schema(description = "待验收")
    private Long pendingAcceptanceCount;

    @Schema(description = "已完成")
    private Long completedOrderCount;

    @Schema(description = "已取消")
    private Long canceledOrderCount;

    @Schema(description = "售后处理中")
    private Long afterSaleOrderCount;

    // ====== 金额统计 ======

    @Schema(description = "服务订单总金额（分）")
    private Long totalServiceAmount;

    @Schema(description = "已完成服务金额（分）")
    private Long completedServiceAmount;

    // ====== 售后与资金 ======

    @Schema(description = "售后案件数")
    private Long afterSaleCount;

    @Schema(description = "退款笔数")
    private Long refundCount;

    @Schema(description = "退款总金额（分）")
    private Long refundAmount;

    @Schema(description = "追回任务数")
    private Long recoveryTaskCount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

    // ====== 结算 ======

    @Schema(description = "打手结算笔数")
    private Long workerSettlementCount;

    @Schema(description = "结算总金额（分）")
    private Long settlementAmount;

    @Schema(description = "已打款金额（分）")
    private Long paidSettlementAmount;

}
