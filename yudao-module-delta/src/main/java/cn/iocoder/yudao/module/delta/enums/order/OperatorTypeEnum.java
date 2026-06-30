package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 操作人类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum OperatorTypeEnum implements ArrayValuable<Integer> {

    CUSTOMER(1, "客户"),
    WORKER(2, "打手"),
    ADMIN(3, "客服"),
    SYSTEM(4, "系统");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OperatorTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
