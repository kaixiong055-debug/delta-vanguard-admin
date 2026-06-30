package cn.iocoder.yudao.module.delta.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaEventOutboxMapper;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventOutboxConsumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Outbox 事件消费任务
 * <p>
 * 每分钟扫描一次，消费待处理和可重试的失败事件。
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaEventOutboxConsumeJob implements JobHandler {

    /** 每次最多处理的事件数 */
    private static final int BATCH_SIZE = 50;

    @Resource
    private DeltaEventOutboxMapper deltaEventOutboxMapper;
    @Resource
    private DeltaEventOutboxConsumeService deltaEventOutboxConsumeService;

    @Override
    public String execute(String param) throws Exception {
        int totalProcessed = 0;
        int totalSuccess = 0;

        // 1. 恢复长时间 PROCESSING 的事件
        deltaEventOutboxConsumeService.recoverStuckEvents(20);

        // 2. 消费待处理事件
        List<DeltaEventOutboxDO> events = deltaEventOutboxMapper.selectPendingEvents(BATCH_SIZE);
        for (DeltaEventOutboxDO event : events) {
            boolean success = deltaEventOutboxConsumeService.processEvent(event);
            totalProcessed++;
            if (success) {
                totalSuccess++;
            }
        }

        String result = String.format("Outbox消费完成: 处理%d 成功%d", totalProcessed, totalSuccess);
        log.info(result);
        return result;
    }
}
