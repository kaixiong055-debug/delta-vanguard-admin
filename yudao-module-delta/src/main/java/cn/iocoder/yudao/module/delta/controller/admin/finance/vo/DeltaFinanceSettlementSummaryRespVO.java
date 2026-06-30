package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 结算汇总响应 VO
 */
@Schema(description = "管理后台 - 结算汇总")
@Data
public class DeltaFinanceSettlementSummaryRespVO {

    @Schema(description = "结算总笔数")
    private Long totalCount;

    @Schema(description = "待审核")
    private Long pendingCount;

    @Schema(description = "已通过")
    private Long approvedCount;

    @Schema(description = "已驳回")
    private Long rejectedCount;

    @Schema(description = "已打款")
    private Long paidCount;

    @Schema(description = "已取消")
    private Long canceledCount;

    @Schema(description = "结算总金额（分）")
    private Long totalAmount;

    @Schema(description = "已通过金额（分）")
    private Long approvedAmount;

    @Schema(description = "已打款金额（分）")
    private Long paidAmount;

    @Schema(description = "待审核金额（分）")
    private Long pendingAmount;

}
