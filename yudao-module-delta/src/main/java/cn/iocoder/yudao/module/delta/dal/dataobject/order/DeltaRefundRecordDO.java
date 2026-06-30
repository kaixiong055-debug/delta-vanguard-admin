package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 内部退款记录 DO（不调用真实支付接口）
 *
 * @author Delta-Vanguard
 */
@TableName("delta_refund_record")
@KeySequence("delta_refund_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaRefundRecordDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 退款单号 */
    private String refundNo;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 售后案件ID */
    private Long afterSaleId;
    /** 买家用户ID */
    private Long buyerUserId;
    /** 退款金额（分） */
    private Integer refundAmount;
    /** 退款原因 */
    private String refundReason;
    /** 退款状态：0-待人工退款 1-人工退款处理中 2-人工退款已完成 3-已取消 4-人工退款失败 */
    private Integer refundStatus;
    /** 退款渠道 */
    private String refundChannel;
    /** 外部退款流水号 */
    private String externalRefundNo;
    /** 操作人ID（创建人） */
    private Long operatorId;
    /** 备注 */
    private String remark;
    // ========== Phase 9 新增字段 ==========
    /** 处理人ID */
    private Long handlerId;
    /** 开始处理时间 */
    private java.time.LocalDateTime handleTime;
    /** 退款完成时间 */
    private java.time.LocalDateTime completedTime;
    /** 退款失败时间 */
    private java.time.LocalDateTime failedTime;
    /** 人工退款方式：1-人工微信 2-银行卡 3-支付宝 4-其他 */
    private Integer refundMethod;
    /** 外部参考号（人工转账流水号） */
    private String externalReference;
    /** 退款失败原因 */
    private String failureReason;
    /** 凭证URL列表（JSON数组） */
    private String proofUrls;
    /** 处理备注 */
    private String processRemark;

}
