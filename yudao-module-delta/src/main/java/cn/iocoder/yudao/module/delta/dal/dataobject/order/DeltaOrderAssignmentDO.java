package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 派单记录 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_assignment")
@KeySequence("delta_order_assignment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderAssignmentDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 服务订单ID（关联 delta_service_order.id）
     */
    private Long serviceOrderId;
    /**
     * 打手ID（关联 delta_worker.id）
     */
    private Long workerId;
    /**
     * 派单类型：1-客户指定 2-客服派单 3-大厅抢单 4-改派 5-俱乐部分派
     */
    private Integer assignmentType;
    /**
     * 派单状态：1-待确认 2-已接受 3-已拒绝 4-已超时
     */
    private Integer assignmentStatus;
    /**
     * 操作人类型：1-客户 2-打手 3-客服 4-系统 5-俱乐部负责人
     */
    private Integer operatorType;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作原因/备注
     */
    private String reason;
    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
    /**
     * 接受/拒绝时间
     */
    private LocalDateTime acceptedAt;

}
