package cn.iocoder.yudao.module.delta.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 渠道发送请求 DTO
 *
 * @author Delta-Vanguard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaChannelSendRequest {

    /** 租户ID */
    private Long tenantId;

    /** 关联 Outbox 事件ID */
    private Long eventOutboxId;

    /** 事件编号 */
    private String eventNo;

    /** 事件类型 */
    private String eventType;

    /** 接收人类型 */
    private String recipientType;

    /** 接收人用户ID */
    private Long recipientId;

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    private String content;

    /** 聚合类型 */
    private String aggregateType;

    /** 聚合根ID */
    private Long aggregateId;

    /** 模板参数 */
    private Map<String, String> templateParams;

}
