package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 服务类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ServiceTypeEnum implements ArrayValuable<Integer> {

    ESCORT(1, "陪玩"),
    CONVOY(2, "护航"),
    FUN(3, "趣味单");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ServiceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ServiceTypeEnum valueOf(Integer type) {
        if (type == null) return null;
        for (ServiceTypeEnum e : values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

}
