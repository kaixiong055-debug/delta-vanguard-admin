package cn.iocoder.yudao.module.delta.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 打手结算 DO（Phase 7 审核流程）
 * <p>
 * 抽成比例为万分制：1500 = 15.00%
 *
 * @author Delta-Vanguard
 */
@TableName("delta_worker_settlement")
@KeySequence("delta_worker_settlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerSettlementDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 结算单号 */
    private String settlementNo;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 打手ID */
    private Long workerId;
    /** 服务金额（分） */
    private Integer serviceAmount;
    /** 抽成比例（万分制，快照，如1500表示15.00%） */
    private Integer commissionRate;
    /** 平台抽成金额（分） */
    private Integer platformFee;
    /** 打手收入金额（分） */
    private Integer workerAmount;
    /** 结算状态：0-待审核 1-审核通过 2-审核驳回 3-已打款 4-已取消 */
    private Integer settlementStatus;
    /** 结算完成时间（验收时间） */
    private LocalDateTime settledAt;
    /** 打款渠道 */
    private String payChannel;
    /** 打款流水号/参考号 */
    private String payReference;
    /** 审核人ID（AdminUser） */
    private Long reviewerId;
    /** 审核时间 */
    private LocalDateTime reviewTime;
    /** 驳回原因 */
    private String rejectReason;
    /** 打款人ID（AdminUser） */
    private Long payerId;
    /** 打款时间 */
    private LocalDateTime paidTime;
    /** 打款方式 */
    private Integer payMethod;
    /** 打款备注 */
    private String payRemark;
    /** 备注 */
    private String remark;

}
