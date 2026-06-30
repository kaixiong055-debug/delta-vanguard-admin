package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 派单状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum AssignmentStatusEnum implements ArrayValuable<Integer> {

    PENDING(1, "待确认"),
    ACCEPTED(2, "已接受"),
    REJECTED(3, "已拒绝"),
    EXPIRED(4, "已超时"),
    CANCELED(5, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AssignmentStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isActive(Integer status) {
        return ACCEPTED.status.equals(status) || PENDING.status.equals(status);
    }

}
