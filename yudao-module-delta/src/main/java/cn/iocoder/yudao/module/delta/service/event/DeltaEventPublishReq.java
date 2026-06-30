package cn.iocoder.yudao.module.delta.service.event;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 事件发布请求
 *
 * @author Delta-Vanguard
 */
@Data
@Builder
public class DeltaEventPublishReq {

    /** 事件类型 */
    private String eventType;

    /** 租户ID */
    private Long tenantId;

    /** 聚合类型 */
    private String aggregateType;

    /** 聚合根ID */
    private Long aggregateId;

    /** 幂等业务键 */
    private String bizKey;

    /** 接收人类型 */
    private String recipientType;

    /** 接收人用户ID */
    private Long recipientId;

    /** 事件参数快照 */
    private DeltaEventPayload payload;

    /** 通知模板编码 */
    private String templateCode;

    /** 模板渲染参数 */
    private Map<String, String> templateParams;
}
