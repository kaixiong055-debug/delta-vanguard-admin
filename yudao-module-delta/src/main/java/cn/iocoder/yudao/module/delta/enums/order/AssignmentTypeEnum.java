package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 派单类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum AssignmentTypeEnum implements ArrayValuable<Integer> {

    DESIGNATED(1, "客户指定"),
    ADMIN_ASSIGN(2, "客服派单"),
    PUBLIC_CLAIM(3, "大厅抢单"),
    REASSIGN(4, "改派");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AssignmentTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
