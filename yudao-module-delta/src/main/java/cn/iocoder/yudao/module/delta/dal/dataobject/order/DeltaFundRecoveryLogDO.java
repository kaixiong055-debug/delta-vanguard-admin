package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 追回操作日志 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_fund_recovery_log")
@KeySequence("delta_fund_recovery_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaFundRecoveryLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 追回任务ID */
    private Long recoveryId;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 结算记录ID */
    private Long settlementId;
    /** 操作类型：GENERATE/START/RECORD_PARTIAL/RECORD_COMPLETE/FAIL/RETRY/CANCEL */
    private String operationType;
    /** 操作前状态 */
    private Integer beforeStatus;
    /** 操作后状态 */
    private Integer afterStatus;
    /** 操作人ID */
    private Long operatorId;
    /** 日志内容 */
    private String content;
    /** 本次追回金额（分） */
    private Integer amount;
    /** 累计已追回金额（分） */
    private Integer totalRecoveredAmount;
    /** 剩余金额（分） */
    private Integer remainingAmount;

}
