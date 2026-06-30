package cn.iocoder.yudao.module.delta.dal.dataobject.finance;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务对账记录 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_finance_reconciliation")
@KeySequence("delta_finance_reconciliation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaFinanceReconciliationDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 对账单号 */
    private String reconciliationNo;

    /** 对账日期 */
    private LocalDate reconciliationDate;

    /** 对账周期开始时间 */
    private LocalDateTime periodStartTime;

    /** 对账周期结束时间 */
    private LocalDateTime periodEndTime;

    // ====== 服务订单汇总 ======

    /** 服务订单数量 */
    private Integer serviceOrderCount;

    /** 服务订单总金额（分） */
    private Long serviceOrderAmount;

    // ====== 结算汇总 ======

    /** 结算笔数 */
    private Integer settlementCount;

    /** 结算总金额（分） */
    private Long settlementAmount;

    /** 已打款金额（分） */
    private Long paidSettlementAmount;

    // ====== 退款汇总 ======

    /** 退款笔数 */
    private Integer refundCount;

    /** 退款总金额（分） */
    private Long refundAmount;

    // ====== 追回汇总 ======

    /** 追回任务数 */
    private Integer recoveryCount;

    /** 应追回金额（分） */
    private Long shouldRecoverAmount;

    /** 已追回金额（分） */
    private Long recoveredAmount;

    // ====== 平台金额 ======

    /** 预期平台收入（分） */
    private Long expectedPlatformAmount;

    /** 实际平台收入（分） */
    private Long actualPlatformAmount;

    /** 差异金额（分）：actual - expected */
    private Long differenceAmount;

    // ====== 状态 ======

    /** 状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消 */
    private Integer status;

    /** 失败原因 */
    private String failureReason;

    /** 确认备注 */
    private String confirmRemark;

    /** 确认人ID */
    private Long confirmerId;

    /** 确认时间 */
    private LocalDateTime confirmedTime;

    /** 计算完成时间 */
    private LocalDateTime calculateTime;

    /** 乐观锁版本号 */
    private Integer version;

}
