package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Outbox 事件状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum EventOutboxStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    DEAD(4, "死亡（超过最大重试次数）"),
    ;

    private final Integer status;
    private final String description;

    public static boolean canConsume(Integer status) {
        return PENDING.getStatus().equals(status) || FAILED.getStatus().equals(status);
    }

    public static boolean canRetry(Integer status) {
        return FAILED.getStatus().equals(status);
    }

    public static boolean canMarkDead(Integer status) {
        return FAILED.getStatus().equals(status) || PENDING.getStatus().equals(status);
    }

    private static final Set<Integer> FINAL_STATUSES = Arrays.stream(values())
            .filter(e -> e == SUCCESS || e == DEAD)
            .map(EventOutboxStatusEnum::getStatus)
            .collect(Collectors.toSet());

    public static boolean isFinalStatus(Integer status) {
        return FINAL_STATUSES.contains(status);
    }
}
