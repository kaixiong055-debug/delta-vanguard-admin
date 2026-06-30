package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 责任归属类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ResponsibilityTypeEnum implements ArrayValuable<Integer> {

    BUYER(0, "买家责任"),
    WORKER(1, "打手责任"),
    PLATFORM(2, "平台责任"),
    SHARED(3, "共同责任"),
    NONE(4, "无责任");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ResponsibilityTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
