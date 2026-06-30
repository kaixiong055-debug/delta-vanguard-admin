package cn.iocoder.yudao.module.delta.enums.club;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 俱乐部入驻申请状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaClubApplicationStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    CANCELED(3, "已撤销");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DeltaClubApplicationStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPending(Integer status) {
        return Objects.equals(PENDING.getStatus(), status);
    }

    public static boolean isApproved(Integer status) {
        return Objects.equals(APPROVED.getStatus(), status);
    }

    public static boolean isRejected(Integer status) {
        return Objects.equals(REJECTED.getStatus(), status);
    }

    public static boolean isCanceled(Integer status) {
        return Objects.equals(CANCELED.getStatus(), status);
    }

    /** 是否为终态 */
    public static boolean isTerminal(Integer status) {
        return isApproved(status) || isRejected(status) || isCanceled(status);
    }

}
