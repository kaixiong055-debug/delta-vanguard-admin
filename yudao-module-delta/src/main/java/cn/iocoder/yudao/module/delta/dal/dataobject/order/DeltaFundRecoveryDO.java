package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 人工追回任务 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_fund_recovery")
@KeySequence("delta_fund_recovery_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaFundRecoveryDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 追回单号 */
    private String recoveryNo;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 售后案件ID */
    private Long afterSaleId;
    /** 仲裁记录ID */
    private Long arbitrationId;
    /** 结算记录ID */
    private Long settlementId;
    /** 打手ID */
    private Long workerId;
    /** 责任归属 */
    private Integer responsibilityType;
    /** 应追回金额（分） */
    private Integer shouldRecoverAmount;
    /** 已追回金额（分） */
    private Integer recoveredAmount;
    /** 剩余追回金额（分） */
    private Integer remainingAmount;
    /** 追回状态：0-待处理 1-处理中 2-部分追回 3-已全部追回 4-追回失败 5-已取消 */
    private Integer recoveryStatus;
    /** 追回方式 */
    private Integer recoveryMethod;
    /** 外部参考号 */
    private String externalReference;
    /** 凭证URL列表（JSON数组） */
    private String proofUrls;
    /** 处理人ID */
    private Long handlerId;
    /** 开始处理时间 */
    private LocalDateTime handleTime;
    /** 追回完成时间 */
    private LocalDateTime completedTime;
    /** 追回失败原因 */
    private String failureReason;
    /** 备注 */
    private String remark;

}
