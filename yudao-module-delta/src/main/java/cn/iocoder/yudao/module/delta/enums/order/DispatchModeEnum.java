package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 派单方式枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DispatchModeEnum implements ArrayValuable<Integer> {

    DESIGNATED(1, "客户指定"),
    ADMIN_ASSIGN(2, "客服派单"),
    PUBLIC_CLAIM(3, "接单大厅");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DispatchModeEnum::getMode).toArray(Integer[]::new);

    private final Integer mode;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
