package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 退款操作日志 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_refund_log")
@KeySequence("delta_refund_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaRefundLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 退款记录ID */
    private Long refundRecordId;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 售后案件ID */
    private Long afterSaleId;
    /** 操作类型：CREATE/START/COMPLETE/FAIL/RETRY/CANCEL */
    private String operationType;
    /** 操作前状态 */
    private Integer beforeStatus;
    /** 操作后状态 */
    private Integer afterStatus;
    /** 操作人类型：ADMIN/SYSTEM */
    private String operatorType;
    /** 操作人ID */
    private Long operatorId;
    /** 日志内容 */
    private String content;
    /** 金额快照 */
    private Integer amountSnapshot;

}
