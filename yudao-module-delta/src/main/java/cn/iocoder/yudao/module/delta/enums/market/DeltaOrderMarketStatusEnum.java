package cn.iocoder.yudao.module.delta.enums.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台订单市场挂牌状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaOrderMarketStatusEnum {

    AVAILABLE(0, "可抢"),
    CLAIMED(1, "已被接单"),
    WITHDRAWN(2, "已撤回"),
    EXPIRED(3, "已过期"),
    CLOSED(4, "已关闭");

    private final Integer status;
    private final String name;

    private static final Map<Integer, DeltaOrderMarketStatusEnum> STATUS_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(DeltaOrderMarketStatusEnum::getStatus, e -> e));

    public static DeltaOrderMarketStatusEnum fromStatus(Integer status) {
        return STATUS_MAP.get(status);
    }

    public static boolean isValid(Integer status) {
        return STATUS_MAP.containsKey(status);
    }

    /**
     * 是否为终态（不可再变更）
     */
    public boolean isFinal() {
        return this == CLAIMED || this == WITHDRAWN || this == EXPIRED || this == CLOSED;
    }
}
