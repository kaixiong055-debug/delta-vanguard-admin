package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 进度类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ProgressTypeEnum implements ArrayValuable<Integer> {

    START_SERVICE(1, "开始服务"),
    PROGRESS_UPDATE(2, "进度更新"),
    EXCEPTION_REPORT(3, "异常报告"),
    SUBMIT_COMPLETION(4, "提交完成"),
    REWORK_REQUEST(5, "要求返工"),
    ACCEPTANCE_PASSED(6, "验收通过");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ProgressTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
