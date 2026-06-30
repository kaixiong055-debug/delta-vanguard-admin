package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 返工记录 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_rework")
@KeySequence("delta_order_rework_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderReworkDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 服务订单ID
     */
    private Long serviceOrderId;
    /**
     * 打手ID
     */
    private Long workerId;
    /**
     * 返工序号（第几次返工）
     */
    private Integer reworkNo;
    /**
     * 返工原因
     */
    private String reason;
    /**
     * 操作人类型：1-客户 2-打手 3-客服 4-系统
     */
    private Integer operatorType;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    private Integer afterStatus;

}
