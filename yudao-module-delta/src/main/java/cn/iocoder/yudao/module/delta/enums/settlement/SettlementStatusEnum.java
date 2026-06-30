package cn.iocoder.yudao.module.delta.enums.settlement;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 结算状态枚举（Phase 7 审核流程）
 * <p>
 * 抽成比例为万分制：1500 = 15.00%
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum SettlementStatusEnum implements ArrayValuable<Integer> {

    /** 待审核（验收完成后自动生成） */
    PENDING_REVIEW(0, "待审核"),
    /** 审核通过，待打款 */
    APPROVED(1, "审核通过"),
    /** 审核驳回 */
    REJECTED(2, "审核驳回"),
    /** 已打款（后台人工确认） */
    PAID(3, "已打款"),
    /** 已取消 */
    CANCELED(4, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(SettlementStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPendingReview(Integer status) {
        return PENDING_REVIEW.status.equals(status);
    }

    public static boolean isApproved(Integer status) {
        return APPROVED.status.equals(status);
    }

    public static boolean isRejected(Integer status) {
        return REJECTED.status.equals(status);
    }

    public static boolean isPaid(Integer status) {
        return PAID.status.equals(status);
    }

}
