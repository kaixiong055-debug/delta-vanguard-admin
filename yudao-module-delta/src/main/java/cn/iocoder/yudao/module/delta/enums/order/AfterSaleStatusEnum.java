package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 售后案件状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum AfterSaleStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待处理"),
    ACCEPTED(1, "已受理"),
    REJECTED(2, "已驳回"),
    ARBITRATED(3, "已仲裁"),
    CLOSED(4, "已关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AfterSaleStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPending(Integer status) {
        return PENDING.status.equals(status);
    }

    public static boolean isAccepted(Integer status) {
        return ACCEPTED.status.equals(status);
    }

}
