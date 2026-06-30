package cn.iocoder.yudao.module.delta.service.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Delta 领域事件 Payload DTO
 *
 * @author Delta-Vanguard
 */
@Data
@Builder
public class DeltaEventPayload {

    /** 事件类型 */
    private String eventType;

    /** 租户ID */
    private Long tenantId;

    /** 聚合根ID */
    private Long aggregateId;

    /** 服务订单ID */
    private Long serviceOrderId;

    /** 服务订单号 */
    private String serviceOrderNo;

    /** 买家用户ID */
    private Long buyerUserId;

    /** 打手ID（delta_worker.id） */
    private Long workerId;

    /** 打手用户ID（member_user.id） */
    private Long workerUserId;

    /** 操作人类型 */
    private Integer operatorType;

    /** 操作人ID */
    private Long operatorId;

    /** 操作前状态 */
    private Integer beforeStatus;

    /** 操作后状态 */
    private Integer afterStatus;

    /** 模板参数（如 orderNo, reason, amount 等） */
    private Map<String, String> titleArgs;

    /** 模板内容参数 */
    private Map<String, String> contentArgs;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;
}
