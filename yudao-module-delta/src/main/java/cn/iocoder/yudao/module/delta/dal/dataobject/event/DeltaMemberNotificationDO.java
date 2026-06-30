package cn.iocoder.yudao.module.delta.dal.dataobject.event;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Delta 会员站内通知
 *
 * @author Delta-Vanguard
 */
@TableName("delta_member_notification")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaMemberNotificationDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID（member_user.id） */
    private Long userId;

    /** 用户类型（BUYER/WORKER，接收时的角色） */
    private String userType;

    /** 通知类型（SYSTEM/ORDER/SETTLEMENT/REFUND/REMINDER） */
    private String notificationType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 业务类型（SERVICE_ORDER/SETTLEMENT/REFUND等） */
    private String bizType;

    /** 业务ID */
    private Long bizId;

    /** 是否已读（0未读 1已读） */
    private Boolean readStatus;

    /** 已读时间 */
    private LocalDateTime readTime;

    /** 关联的Outbox事件ID */
    private Long outboxEventId;

    /** 租户ID */
    private Long tenantId;
}
