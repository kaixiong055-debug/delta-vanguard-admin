package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 售后申请类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum AfterSaleTypeEnum implements ArrayValuable<Integer> {

    QUALITY_ISSUE(1, "服务质量问题"),
    NOT_AS_DESCRIBED(2, "服务与描述不符"),
    WORKER_NO_SHOW(3, "打手未履约"),
    OTHER(4, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AfterSaleTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
