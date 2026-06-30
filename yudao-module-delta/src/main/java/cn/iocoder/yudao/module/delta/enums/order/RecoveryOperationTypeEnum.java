package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 追回操作日志类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RecoveryOperationTypeEnum implements ArrayValuable<String> {

    GENERATE("GENERATE", "生成追回任务"),
    START("START", "开始追回"),
    RECORD_PARTIAL("RECORD_PARTIAL", "记录部分追回"),
    RECORD_COMPLETE("RECORD_COMPLETE", "记录全部追回"),
    FAIL("FAIL", "追回失败"),
    RETRY("RETRY", "重试追回"),
    CANCEL("CANCEL", "取消追回任务");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(RecoveryOperationTypeEnum::getType).toArray(String[]::new);

    private final String type;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
