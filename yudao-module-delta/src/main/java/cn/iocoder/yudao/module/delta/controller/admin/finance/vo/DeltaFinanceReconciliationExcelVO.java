package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务对账 Excel VO
 *
 * @author Delta-Vanguard
 */
@Data
public class DeltaFinanceReconciliationExcelVO {

    @ExcelProperty("对账单号")
    private String reconciliationNo;

    @ExcelProperty("对账日期")
    private LocalDate reconciliationDate;

    @ExcelProperty("对账周期开始")
    private LocalDateTime periodStartTime;

    @ExcelProperty("对账周期结束")
    private LocalDateTime periodEndTime;

    @ExcelProperty("服务订单数")
    private Integer serviceOrderCount;

    @ExcelProperty("服务订单金额（分）")
    private Long serviceOrderAmount;

    @ExcelProperty("结算笔数")
    private Integer settlementCount;

    @ExcelProperty("结算金额（分）")
    private Long settlementAmount;

    @ExcelProperty("已打款金额（分）")
    private Long paidSettlementAmount;

    @ExcelProperty("退款笔数")
    private Integer refundCount;

    @ExcelProperty("退款金额（分）")
    private Long refundAmount;

    @ExcelProperty("追回笔数")
    private Integer recoveryCount;

    @ExcelProperty("应追回金额（分）")
    private Long shouldRecoverAmount;

    @ExcelProperty("已追回金额（分）")
    private Long recoveredAmount;

    @ExcelProperty("预期平台收入（分）")
    private Long expectedPlatformAmount;

    @ExcelProperty("实际平台收入（分）")
    private Long actualPlatformAmount;

    @ExcelProperty("差异金额（分）")
    private Long differenceAmount;

    @ExcelProperty("状态")
    private String statusName;

    @ExcelProperty("失败原因")
    private String failureReason;

    @ExcelProperty("确认备注")
    private String confirmRemark;

    @ExcelProperty("确认时间")
    private LocalDateTime confirmedTime;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
