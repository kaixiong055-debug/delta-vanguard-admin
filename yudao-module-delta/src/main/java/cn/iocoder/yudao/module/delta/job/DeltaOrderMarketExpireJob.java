package cn.iocoder.yudao.module.delta.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketLogMapper;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单市场过期处理定时任务
 * <p>
 * 扫描 AVAILABLE 且 expireTime <= now 的挂牌，CAS 更新为 EXPIRED。
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaOrderMarketExpireJob implements JobHandler {

    private static final int BATCH_SIZE = 50;

    @Resource
    private DeltaOrderMarketListingMapper deltaOrderMarketListingMapper;

    @Resource
    private DeltaOrderMarketLogMapper deltaOrderMarketLogMapper;

    @Resource
    private DeltaEventPublisher deltaEventPublisher;

    @Override
    public String execute(String param) throws Exception {
        int totalExpired = 0;
        int totalSkipped = 0;

        LocalDateTime now = LocalDateTime.now();
        List<DeltaOrderMarketListingDO> batch;

        while (true) {
            batch = deltaOrderMarketListingMapper.selectExpiredAvailable(now, BATCH_SIZE);
            if (batch.isEmpty()) {
                break;
            }

            for (DeltaOrderMarketListingDO listing : batch) {
                // CAS 更新为 EXPIRED（幂等）
                int rows = deltaOrderMarketListingMapper.updateStatusCas(
                        listing.getId(), listing.getVersion(),
                        DeltaOrderMarketStatusEnum.AVAILABLE.getStatus(),
                        DeltaOrderMarketStatusEnum.EXPIRED.getStatus());

                if (rows > 0) {
                    // 写日志
                    DeltaOrderMarketLogDO logDO = DeltaOrderMarketLogDO.builder()
                            .listingId(listing.getId())
                            .serviceOrderId(listing.getServiceOrderId())
                            .operationType(DeltaOrderMarketOperationTypeEnum.EXPIRE.getType())
                            .operatorType("SYSTEM")
                            .beforeStatus(DeltaOrderMarketStatusEnum.AVAILABLE.getStatus())
                            .afterStatus(DeltaOrderMarketStatusEnum.EXPIRED.getStatus())
                            .success(1)
                            .remark("挂牌过期自动关闭")
                            .build();
                    deltaOrderMarketLogMapper.insert(logDO);

                    // 发布过期事件（bizKey不含时间戳，利用幂等去重）
                    try {
                        DeltaEventPublishReq eventReq = DeltaEventPublishReq.builder()
                                .eventType(DeltaEventTypeEnum.ORDER_MARKET_EXPIRED.getType())
                                .aggregateType("ORDER_MARKET_LISTING")
                                .aggregateId(listing.getId())
                                .tenantId(listing.getSourceTenantId())
                                .bizKey("market:" + listing.getListingNo() + ":EXPIRE")
                                .build();
                        deltaEventPublisher.publishToSystem(eventReq);
                    } catch (Exception e) {
                        log.error("发布过期事件失败 listingNo={}", listing.getListingNo(), e);
                    }

                    totalExpired++;
                    log.info("挂牌过期处理成功 listingNo={}", listing.getListingNo());
                } else {
                    totalSkipped++;
                }
            }
        }

        String result = String.format("订单市场过期处理完成: 过期%d, 跳过(已变化)%d", totalExpired, totalSkipped);
        log.info(result);
        return result;
    }
}
