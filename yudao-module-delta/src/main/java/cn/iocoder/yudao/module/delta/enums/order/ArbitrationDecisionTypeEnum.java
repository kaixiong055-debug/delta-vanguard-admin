package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 仲裁决定类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ArbitrationDecisionTypeEnum implements ArrayValuable<Integer> {

    NO_REFUND(0, "不退款"),
    FULL_REFUND(1, "全额退款"),
    PARTIAL_REFUND(2, "部分退款"),
    CONTINUE_SERVICE(3, "继续服务");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ArbitrationDecisionTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
