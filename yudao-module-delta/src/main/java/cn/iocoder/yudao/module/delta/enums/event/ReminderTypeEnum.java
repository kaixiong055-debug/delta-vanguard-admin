package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 超时提醒类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ReminderTypeEnum {

    DISPATCH_PENDING("DISPATCH_PENDING", "待派单超时"),
    WORKER_START_PENDING("WORKER_START_PENDING", "打手待开始超时"),
    PROGRESS_SILENT("PROGRESS_SILENT", "服务无进度超时"),
    ACCEPTANCE_PENDING("ACCEPTANCE_PENDING", "待验收超时"),
    REFUND_PENDING("REFUND_PENDING", "退款待处理超时"),
    RECOVERY_PENDING("RECOVERY_PENDING", "追回待处理超时"),
    ;

    private final String type;
    private final String description;

    private static final Map<String, ReminderTypeEnum> TYPE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ReminderTypeEnum::getType, e -> e));

    public static ReminderTypeEnum fromType(String type) {
        return TYPE_MAP.get(type);
    }

    /**
     * 业务类型（用于 Outbox/聚合）
     */
    public String getBizType() {
        return "REMINDER";
    }
}
