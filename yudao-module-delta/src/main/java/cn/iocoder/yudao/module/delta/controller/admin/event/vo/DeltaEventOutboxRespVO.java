package cn.iocoder.yudao.module.delta.controller.admin.event.vo;

import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Admin - Outbox 事件响应 VO
 */
@Schema(description = "管理后台 - Outbox 事件 Response VO")
@Data
public class DeltaEventOutboxRespVO {

    @Schema(description = "事件ID", example = "1")
    private Long id;

    @Schema(description = "事件编号", example = "DEV202601010000001")
    private String eventNo;

    @Schema(description = "事件类型", example = "SERVICE_ORDER_DISPATCHED")
    private String eventType;

    @Schema(description = "聚合类型", example = "SERVICE_ORDER")
    private String aggregateType;

    @Schema(description = "聚合根ID", example = "1")
    private Long aggregateId;

    @Schema(description = "业务键", example = "SERVICE_ORDER_DISPATCHED:1:2:3")
    private String bizKey;

    @Schema(description = "接收人类型", example = "WORKER")
    private String recipientType;

    @Schema(description = "接收人ID", example = "100")
    private Long recipientId;

    @Schema(description = "事件状态", example = "2")
    private Integer eventStatus;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "最近错误摘要")
    private String lastError;

    @Schema(description = "通知模板编码")
    private String templateCode;

    @Schema(description = "事件参数JSON（脱敏后）")
    private String payload;

    @Schema(description = "处理完成时间")
    private LocalDateTime processedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    public static DeltaEventOutboxRespVO from(DeltaEventOutboxDO event) {
        DeltaEventOutboxRespVO vo = new DeltaEventOutboxRespVO();
        vo.setId(event.getId());
        vo.setEventNo(event.getEventNo());
        vo.setEventType(event.getEventType());
        vo.setAggregateType(event.getAggregateType());
        vo.setAggregateId(event.getAggregateId());
        vo.setBizKey(event.getBizKey());
        vo.setRecipientType(event.getRecipientType());
        vo.setRecipientId(event.getRecipientId());
        vo.setEventStatus(event.getEventStatus());
        vo.setRetryCount(event.getRetryCount());
        vo.setNextRetryTime(event.getNextRetryTime());
        vo.setLastError(event.getLastError() != null && event.getLastError().length() > 200
                ? event.getLastError().substring(0, 197) + "..."
                : event.getLastError());
        vo.setTemplateCode(event.getTemplateCode());
        // payload脱敏：只显示前300字符
        if (event.getPayload() != null) {
            vo.setPayload(event.getPayload().length() > 300
                    ? event.getPayload().substring(0, 297) + "..."
                    : event.getPayload());
        }
        vo.setProcessedTime(event.getProcessedTime());
        vo.setCreateTime(event.getCreateTime());
        vo.setTenantId(event.getTenantId());
        return vo;
    }
}
