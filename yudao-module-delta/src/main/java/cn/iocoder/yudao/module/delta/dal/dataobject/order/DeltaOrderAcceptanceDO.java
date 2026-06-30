package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 验收记录 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_acceptance")
@KeySequence("delta_order_acceptance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderAcceptanceDO extends TenantBaseDO {

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
     * 验收结果：1-验收通过 2-要求返工
     */
    private Integer acceptanceResult;
    /**
     * 操作人类型：1-客户 2-打手 3-客服 4-系统
     */
    private Integer operatorType;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    private Integer afterStatus;
    /**
     * 验收时间
     */
    private LocalDateTime acceptanceTime;

}
