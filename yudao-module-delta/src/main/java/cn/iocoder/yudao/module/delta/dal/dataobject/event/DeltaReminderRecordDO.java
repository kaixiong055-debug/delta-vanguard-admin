package cn.iocoder.yudao.module.delta.dal.dataobject.event;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Delta 提醒记录
 *
 * @author Delta-Vanguard
 */
@TableName("delta_reminder_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaReminderRecordDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提醒类型（DISPATCH_PENDING等） */
    private String reminderType;

    /** 业务类型（SERVICE_ORDER/REFUND等） */
    private String bizType;

    /** 业务ID */
    private Long bizId;

    /** 接收人用户ID */
    private Long recipientId;

    /** 接收人类型（BUYER/WORKER/ADMIN） */
    private String recipientType;

    /** 最后提醒时间 */
    private LocalDateTime lastRemindTime;

    /** 提醒次数 */
    private Integer remindCount;

    /** 租户ID */
    private Long tenantId;
}
