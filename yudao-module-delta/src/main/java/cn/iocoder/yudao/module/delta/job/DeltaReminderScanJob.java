package cn.iocoder.yudao.module.delta.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.delta.service.event.DeltaReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 超时提醒扫描任务
 * <p>
 * 每5-10分钟扫描一次（建议配置cron），检查需要提醒的业务并发送通知。
 *
 * @author Delta-Vanguard
 */
@Component
@Slf4j
public class DeltaReminderScanJob implements JobHandler {

    @Resource
    private DeltaReminderService deltaReminderService;

    @Override
    public String execute(String param) throws Exception {
        log.info("开始超时提醒扫描...");
        deltaReminderService.scanAndRemind();
        log.info("超时提醒扫描完成");
        return "超时提醒扫描完成";
    }
}
