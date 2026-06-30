package cn.iocoder.yudao.module.delta.service.event;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaEventOutboxMapper;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.event.EventOutboxStatusEnum;
import cn.iocoder.yudao.module.delta.enums.event.RecipientTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delta 领域事件发布器
 * <p>
 * 在当前事务中写入 Outbox 表，与业务操作保持同一事务。
 * 不直接发送任何通知。
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaEventPublisher {

    /** 事件编号前缀 */
    private static final String EVENT_NO_PREFIX = "DEV";

    @Resource
    private DeltaEventOutboxMapper deltaEventOutboxMapper;
    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;

    /**
     * 发布事件到 Outbox
     */
    public void publish(DeltaEventPublishReq req) {
        Assert.notNull(req.getEventType(), "事件类型不能为空");
        Assert.notNull(req.getTenantId(), "租户ID不能为空");
        Assert.notNull(req.getBizKey(), "幂等键不能为空");

        DeltaEventTypeEnum eventType = DeltaEventTypeEnum.fromType(req.getEventType());
        if (eventType == null) {
            log.error("不支持的事件类型: {}", req.getEventType());
            throw new IllegalArgumentException("不支持的事件类型: " + req.getEventType());
        }

        // 幂等检查（同一事务内唯一约束也防重复，但这里提前检查作为优化）
        DeltaEventOutboxDO exist = deltaEventOutboxMapper.selectByBizKey(req.getBizKey(), req.getTenantId());
        if (exist != null) {
            log.info("事件已存在，幂等跳过 bizKey={}", req.getBizKey());
            return;
        }

        String eventNo = deltaNoRedisDAO.generateEventNo();

        DeltaEventOutboxDO outbox = DeltaEventOutboxDO.builder()
                .eventNo(eventNo)
                .eventType(req.getEventType())
                .aggregateType(req.getAggregateType())
                .aggregateId(req.getAggregateId())
                .bizKey(req.getBizKey())
                .recipientType(req.getRecipientType())
                .recipientId(req.getRecipientId())
                .payload(req.getPayload() != null ? JSONUtil.toJsonStr(req.getPayload()) : null)
                .eventStatus(EventOutboxStatusEnum.PENDING.getStatus())
                .retryCount(0)
                .nextRetryTime(LocalDateTime.now())
                .templateCode(req.getTemplateCode())
                .templateParams(req.getTemplateParams() != null ? JSONUtil.toJsonStr(req.getTemplateParams()) : null)
                .tenantId(req.getTenantId())
                .build();

        outbox.initEventNo(eventNo);

        try {
            deltaEventOutboxMapper.insert(outbox);
            log.info("事件写入Outbox成功 eventNo={}, eventType={}, bizKey={}, recipientType={}, recipientId={}",
                    eventNo, req.getEventType(), req.getBizKey(), req.getRecipientType(), req.getRecipientId());
        } catch (DuplicateKeyException e) {
            log.info("事件已存在（uniquekey冲突），幂等跳过 bizKey={}", req.getBizKey());
        }
    }

    /**
     * 给买家发布事件
     */
    public void publishToBuyer(DeltaEventPublishReq req) {
        req.setRecipientType(RecipientTypeEnum.BUYER.getType());
        publish(req);
    }

    /**
     * 给打手发布事件
     */
    public void publishToWorker(DeltaEventPublishReq req) {
        req.setRecipientType(RecipientTypeEnum.WORKER.getType());
        publish(req);
    }

    /**
     * 给管理员发布事件
     */
    public void publishToAdmin(DeltaEventPublishReq req) {
        req.setRecipientType(RecipientTypeEnum.ADMIN.getType());
        publish(req);
    }

    /**
     * 给系统发布事件
     */
    public void publishToSystem(DeltaEventPublishReq req) {
        req.setRecipientType(RecipientTypeEnum.SYSTEM.getType());
        publish(req);
    }
}
