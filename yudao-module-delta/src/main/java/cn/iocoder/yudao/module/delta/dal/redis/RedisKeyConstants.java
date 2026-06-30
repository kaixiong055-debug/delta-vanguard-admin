package cn.iocoder.yudao.module.delta.dal.redis;

/**
 * Delta Redis Key 常量
 *
 * @author Delta-Vanguard
 */
public interface RedisKeyConstants {

    /**
     * 打手编号的缓存
     *
     * KEY 格式：delta_worker_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String WORKER_NO = "delta_worker_no:";

    /**
     * 服务订单号的缓存
     *
     * KEY 格式：delta_order_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String SERVICE_ORDER_NO = "delta_order_no:";

    /**
     * 结算单号的缓存
     *
     * KEY 格式：delta_settlement_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String SETTLEMENT_NO = "delta_settlement_no:";

    /**
     * 服务单派单/接单分布式锁
     *
     * KEY 格式：delta:service-order:claim:{tenantId}:{serviceOrderId}
     * VALUE 数据格式：锁
     */
    String SERVICE_ORDER_CLAIM_LOCK = "delta:service-order:claim:%d:%d";

    /**
     * 退款处理分布式锁
     *
     * KEY 格式：delta:refund:process:{tenantId}:{refundId}
     */
    String REFUND_PROCESS_LOCK = "delta:refund:process:%d:%d";

    /**
     * 追回处理分布式锁
     *
     * KEY 格式：delta:recovery:process:{tenantId}:{recoveryId}
     */
    String RECOVERY_PROCESS_LOCK = "delta:recovery:process:%d:%d";

    /**
     * 事件消费分布式锁
     *
     * KEY 格式：delta:event:consume:{tenantId}:{eventId}
     */
    String EVENT_CONSUME_LOCK = "delta:event:consume:%d:%d";

    /**
     * 俱乐部入驻申请提交分布式锁
     *
     * KEY 格式：delta:club-application:submit:{memberId}
     */
    String CLUB_APPLICATION_SUBMIT_LOCK = "delta:club-application:submit:%d";

    /**
     * 订单市场抢单/指定分布式锁
     *
     * KEY 格式：delta:order-market:claim:{listingId}
     */
    String ORDER_MARKET_CLAIM_LOCK = "delta:order-market:claim:%d";

    /**
     * 订单市场发布分布式锁（防止同一服务订单并发创建多个有效挂牌）
     *
     * KEY 格式：delta:order-market:publish:{serviceOrderId}
     */
    String ORDER_MARKET_PUBLISH_LOCK = "delta:order-market:publish:%d";

    /**
     * 订单市场挂牌编号的缓存
     *
     * KEY 格式：delta_market_listing_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String MARKET_LISTING_NO = "delta_market_listing_no:";

}
