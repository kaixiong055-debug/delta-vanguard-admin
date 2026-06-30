package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 退款状态枚举（内部人工退款，不调用真实支付渠道）
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum implements ArrayValuable<Integer> {

    PENDING_MANUAL(0, "待人工退款"),
    MANUAL_PROCESSING(1, "人工退款处理中"),
    MANUAL_COMPLETED(2, "人工退款已完成"),
    CANCELED(3, "已取消"),
    MANUAL_FAILED(4, "人工退款失败");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(RefundStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPendingManual(Integer status) {
        return PENDING_MANUAL.getStatus().equals(status);
    }

    public static boolean isManualProcessing(Integer status) {
        return MANUAL_PROCESSING.getStatus().equals(status);
    }

    public static boolean isManualCompleted(Integer status) {
        return MANUAL_COMPLETED.getStatus().equals(status);
    }

    public static boolean isManualFailed(Integer status) {
        return MANUAL_FAILED.getStatus().equals(status);
    }

    public static boolean isCanceled(Integer status) {
        return CANCELED.getStatus().equals(status);
    }

}
