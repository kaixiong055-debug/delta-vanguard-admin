package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对账记录响应 VO
 */
@Schema(description = "管理后台 - 对账记录")
@Data
public class DeltaFinanceReconciliationRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "对账单号")
    private String reconciliationNo;

    @Schema(description = "对账日期")
    private LocalDate reconciliationDate;

    @Schema(description = "对账周期开始")
    private LocalDateTime periodStartTime;

    @Schema(description = "对账周期结束")
    private LocalDateTime periodEndTime;

    @Schema(description = "服务订单数")
    private Integer serviceOrderCount;

    @Schema(description = "服务订单金额（分）")
    private Long serviceOrderAmount;

    @Schema(description = "结算笔数")
    private Integer settlementCount;

    @Schema(description = "结算金额（分）")
    private Long settlementAmount;

    @Schema(description = "已打款金额（分）")
    private Long paidSettlementAmount;

    @Schema(description = "退款笔数")
    private Integer refundCount;

    @Schema(description = "退款金额（分）")
    private Long refundAmount;

    @Schema(description = "追回笔数")
    private Integer recoveryCount;

    @Schema(description = "应追回金额（分）")
    private Long shouldRecoverAmount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

    @Schema(description = "预期平台收入（分）")
    private Long expectedPlatformAmount;

    @Schema(description = "实际平台收入（分）")
    private Long actualPlatformAmount;

    @Schema(description = "差异金额（分）")
    private Long differenceAmount;

    @Schema(description = "状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "确认备注")
    private String confirmRemark;

    @Schema(description = "确认人ID")
    private Long confirmerId;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedTime;

    @Schema(description = "计算完成时间")
    private LocalDateTime calculateTime;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
