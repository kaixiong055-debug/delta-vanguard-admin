package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 取消申请状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum CancelStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CancelStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPending(Integer status) {
        return PENDING.status.equals(status);
    }

    public static boolean isApproved(Integer status) {
        return APPROVED.status.equals(status);
    }

    public static boolean isRejected(Integer status) {
        return REJECTED.status.equals(status);
    }

}
