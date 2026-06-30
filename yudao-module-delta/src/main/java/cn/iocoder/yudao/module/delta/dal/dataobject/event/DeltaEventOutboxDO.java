package cn.iocoder.yudao.module.delta.dal.dataobject.event;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Delta 领域事件 Outbox
 *
 * @author Delta-Vanguard
 */
@TableName("delta_event_outbox")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaEventOutboxDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 事件编号 */
    private String eventNo;

    /** 事件类型（DeltaEventTypeEnum.type） */
    private String eventType;

    /** 聚合类型（SERVICE_ORDER / SETTLEMENT / REFUND 等） */
    private String aggregateType;

    /** 聚合根ID */
    private Long aggregateId;

    /** 幂等业务键 */
    private String bizKey;

    /** 接收人类型（BUYER / WORKER / ADMIN / SYSTEM） */
    private String recipientType;

    /** 接收人用户ID（member_user.id） */
    private Long recipientId;

    /** 事件参数JSON */
    private String payload;

    /** 事件状态（EventOutboxStatusEnum） */
    private Integer eventStatus;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 最近错误摘要 */
    private String lastError;

    /** 处理完成时间 */
    private LocalDateTime processedTime;

    /** 关联通知模板编码 */
    private String templateCode;

    /** 模板渲染参数JSON */
    private String templateParams;

    /** 租户ID（继承自 TenantBaseDO，显式声明便于MyBatis映射） */
    private Long tenantId;

    public void initEventNo(String eventNo) {
        this.eventNo = eventNo;
        this.eventStatus = 0; // PENDING
        this.retryCount = 0;
    }
}
