package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 仲裁记录 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_after_sale_arbitration")
@KeySequence("delta_after_sale_arbitration_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaAfterSaleArbitrationDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 售后案件ID */
    private Long afterSaleId;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 仲裁决定类型：0-不退款 1-全额退款 2-部分退款 3-继续服务 */
    private Integer decisionType;
    /** 退款金额（分） */
    private Integer refundAmount;
    /** 打手扣减金额（分） */
    private Integer workerDeductionAmount;
    /** 平台承担金额（分） */
    private Integer platformBearAmount;
    /** 责任归属 */
    private Integer responsibilityType;
    /** 操作人ID */
    private Long operatorId;
    /** 仲裁备注 */
    private String remark;
    /** 仲裁前状态 */
    private Integer beforeStatus;
    /** 仲裁后状态 */
    private Integer afterStatus;

}
