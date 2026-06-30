package cn.iocoder.yudao.module.delta.enums.worker;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 打手审核状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum WorkerAuditStatusEnum implements ArrayValuable<Integer> {

    NOT_APPLIED(0, "未申请"),
    PENDING(1, "审核中"),
    APPROVED(2, "审核通过"),
    REJECTED(3, "审核驳回"),
    DISABLED(4, "已停用"),
    BLACKLISTED(5, "已拉黑");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(WorkerAuditStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isApproved(Integer status) {
        return APPROVED.status.equals(status);
    }

    public static boolean isPending(Integer status) {
        return PENDING.status.equals(status);
    }

}
