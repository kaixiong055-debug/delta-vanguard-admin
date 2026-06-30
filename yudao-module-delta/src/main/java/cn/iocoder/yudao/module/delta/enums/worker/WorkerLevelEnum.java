package cn.iocoder.yudao.module.delta.enums.worker;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 打手等级枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum WorkerLevelEnum implements ArrayValuable<Integer> {

    JUNIOR(1, "初级"),
    INTERMEDIATE(2, "中级"),
    SENIOR(3, "高级"),
    EXPERT(4, "资深");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(WorkerLevelEnum::getLevel).toArray(Integer[]::new);

    private final Integer level;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
