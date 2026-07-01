package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 订单日志 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_log")
@KeySequence("delta_order_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 服务订单ID
     */
    private Long serviceOrderId;
    /**
     * 操作人类型：1-客户 2-打手 3-客服 4-系统 5-俱乐部负责人
     */
    private Integer operatorType;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作名称
     */
    private String operation;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    private Integer afterStatus;
    /**
     * 日志内容
     */
    private String content;

}
