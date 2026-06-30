package cn.iocoder.yudao.module.delta.enums.club;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 俱乐部经营状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaClubBusinessStatusEnum implements ArrayValuable<Integer> {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DeltaClubBusinessStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isEnabled(Integer status) {
        return Objects.equals(ENABLED.getStatus(), status);
    }

    public static boolean isDisabled(Integer status) {
        return Objects.equals(DISABLED.getStatus(), status);
    }

}
