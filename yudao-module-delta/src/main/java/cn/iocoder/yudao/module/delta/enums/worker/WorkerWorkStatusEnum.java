package cn.iocoder.yudao.module.delta.enums.worker;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 打手工作状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum WorkerWorkStatusEnum implements ArrayValuable<Integer> {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌"),
    PAUSED(3, "暂停接单");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(WorkerWorkStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static WorkerWorkStatusEnum valueOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

    /**
     * 允许 App 主动切换的状态
     */
    public static boolean isAppChangeable(Integer status) {
        return ONLINE.getStatus().equals(status)
                || OFFLINE.getStatus().equals(status)
                || PAUSED.getStatus().equals(status);
    }

}
