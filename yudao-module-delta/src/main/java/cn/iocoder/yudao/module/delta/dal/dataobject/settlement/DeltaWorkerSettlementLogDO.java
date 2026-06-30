package cn.iocoder.yudao.module.delta.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 结算操作日志 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_worker_settlement_log")
@KeySequence("delta_worker_settlement_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerSettlementLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 结算单ID */
    private Long settlementId;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 操作类型（GENERATE/APPROVE/REJECT/RESUBMIT/MARK_PAID/REVOKE_PAID） */
    private String operationType;
    /** 操作前状态 */
    private Integer beforeStatus;
    /** 操作后状态 */
    private Integer afterStatus;
    /** 操作人类型：1-会员 3-管理员 4-系统 */
    private Integer operatorType;
    /** 操作人ID */
    private Long operatorId;
    /** 操作内容 */
    private String content;
    /** 金额快照JSON（serviceAmount/commissionRate/platformFee/workerAmount） */
    private String amountSnapshot;
    /** 创建时间 */
    private LocalDateTime createTime;

}
