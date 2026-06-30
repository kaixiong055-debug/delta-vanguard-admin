package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 追回状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RecoveryStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    PARTIALLY_RECOVERED(2, "部分追回"),
    RECOVERED(3, "已全部追回"),
    FAILED(4, "追回失败"),
    CANCELED(5, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(RecoveryStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPending(Integer status) {
        return PENDING.getStatus().equals(status);
    }

    public static boolean isProcessing(Integer status) {
        return PROCESSING.getStatus().equals(status);
    }

    public static boolean isPartiallyRecovered(Integer status) {
        return PARTIALLY_RECOVERED.getStatus().equals(status);
    }

    public static boolean isRecovered(Integer status) {
        return RECOVERED.getStatus().equals(status);
    }

    public static boolean isFailed(Integer status) {
        return FAILED.getStatus().equals(status);
    }

    public static boolean isCanceled(Integer status) {
        return CANCELED.getStatus().equals(status);
    }

}
