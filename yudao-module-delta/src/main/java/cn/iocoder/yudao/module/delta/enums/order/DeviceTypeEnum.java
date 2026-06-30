package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum implements ArrayValuable<Integer> {

    MOBILE(1, "手机"),
    TABLET(2, "平板"),
    PC(3, "PC");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DeviceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
