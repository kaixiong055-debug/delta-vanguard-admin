package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 取消申请 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_cancel")
@KeySequence("delta_order_cancel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderCancelDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 取消单号 */
    private String cancelNo;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 买家用户ID */
    private Long buyerUserId;
    /** 打手ID */
    private Long workerId;
    /** 申请原因 */
    private String applyReason;
    /** 申请备注 */
    private String applyRemark;
    /** 取消状态：0-待审核 1-已通过 2-已驳回 */
    private Integer applyStatus;
    /** 原始服务单状态（快照） */
    private Integer originalOrderStatus;
    /** 取消类型：1-买家取消 */
    private Integer cancelType;
    /** 退款金额（分） */
    private Integer refundAmount;
    /** 责任归属 */
    private Integer responsibilityType;
    /** 审核人ID */
    private Long reviewerId;
    /** 审核时间 */
    private LocalDateTime reviewTime;
    /** 审核备注 */
    private String reviewRemark;

}
