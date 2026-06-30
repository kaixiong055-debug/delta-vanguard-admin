package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 验收结果枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum AcceptanceResultEnum implements ArrayValuable<Integer> {

    PASS(1, "验收通过"),
    REWORK_REQUESTED(2, "要求返工");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AcceptanceResultEnum::getResult).toArray(Integer[]::new);

    private final Integer result;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
