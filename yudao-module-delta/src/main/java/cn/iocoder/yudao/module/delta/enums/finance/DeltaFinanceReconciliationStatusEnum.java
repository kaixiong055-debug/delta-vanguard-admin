package cn.iocoder.yudao.module.delta.enums.finance;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * 财务对账状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaFinanceReconciliationStatusEnum implements ArrayValuable<Integer> {

    /** 待计算 */
    PENDING(0, "待计算"),
    /** 对账一致 */
    MATCHED(1, "对账一致"),
    /** 存在差异 */
    DIFFERENCE(2, "存在差异"),
    /** 已确认 */
    CONFIRMED(3, "已确认"),
    /** 计算失败 */
    FAILED(4, "计算失败"),
    /** 已取消 */
    CANCELED(5, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DeltaFinanceReconciliationStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPending(Integer status) {
        return PENDING.status.equals(status);
    }

    public static boolean isMatched(Integer status) {
        return MATCHED.status.equals(status);
    }

    public static boolean isDifference(Integer status) {
        return DIFFERENCE.status.equals(status);
    }

    public static boolean isConfirmed(Integer status) {
        return CONFIRMED.status.equals(status);
    }

    public static boolean isFailed(Integer status) {
        return FAILED.status.equals(status);
    }

    public static boolean isCanceled(Integer status) {
        return CANCELED.status.equals(status);
    }

    /** 终态：不可再变更 */
    public static final Set<Integer> FINAL_STATUSES = Set.of(CONFIRMED.status, CANCELED.status);

    public static boolean isFinalStatus(Integer status) {
        return FINAL_STATUSES.contains(status);
    }

    /** 可确认的状态 */
    public static final Set<Integer> CONFIRMABLE_STATUSES = Set.of(MATCHED.status, DIFFERENCE.status);

    public static boolean canConfirm(Integer status) {
        return CONFIRMABLE_STATUSES.contains(status);
    }

    /** 可重试的状态 */
    public static boolean canRetry(Integer status) {
        return FAILED.status.equals(status);
    }

    /** 可取消的状态 */
    public static final Set<Integer> CANCELABLE_STATUSES = Set.of(PENDING.status, MATCHED.status, DIFFERENCE.status);

    public static boolean canCancel(Integer status) {
        return CANCELABLE_STATUSES.contains(status);
    }

}
