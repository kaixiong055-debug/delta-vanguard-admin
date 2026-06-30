package cn.iocoder.yudao.module.delta.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Delta 超时提醒配置
 *
 * @author Delta-Vanguard
 */
@Component
@ConfigurationProperties(prefix = "yudao.delta.reminder")
@Data
public class DeltaReminderProperties {

    /** 待派单超时（分钟），默认30 */
    private int dispatchPendingMinutes = 30;

    /** 打手待开始超时（分钟），默认30 */
    private int workerStartMinutes = 30;

    /** 服务无进度超时（小时），默认6 */
    private int progressSilentHours = 6;

    /** 待验收超时（小时），默认12 */
    private int acceptancePendingHours = 12;

    /** 退款待处理超时（小时），默认24 */
    private int refundPendingHours = 24;

    /** 追回待处理超时（小时），默认48 */
    private int recoveryPendingHours = 48;

    /** 提醒冷却时间（小时），默认6 */
    private int cooldownHours = 6;
}
