package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 凭证类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum EvidenceTypeEnum implements ArrayValuable<Integer> {

    SCREENSHOT(1, "截图"),
    VIDEO(2, "视频"),
    TEXT(3, "文字说明"),
    FILE(4, "文件");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(EvidenceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
