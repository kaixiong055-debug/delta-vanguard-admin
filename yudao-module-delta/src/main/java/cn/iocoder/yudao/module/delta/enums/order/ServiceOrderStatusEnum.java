package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 服务订单状态枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum ServiceOrderStatusEnum implements ArrayValuable<Integer> {

    PENDING_DISPATCH(10, "待派单"),
    WAITING_DESIGNATED(20, "等待指定打手确认"),
    POOL_PENDING(30, "接单大厅待领取"),
    ACCEPTED_PENDING_START(40, "已接单待开始"),
    IN_PROGRESS(50, "服务进行中"),
    WORKER_SUBMITTED(60, "打手已提交完成"),
    PENDING_VERIFICATION(70, "待客户或客服验收"),
    COMPLETED(80, "已完成"),
    AFTER_SALE(90, "售后处理中"),
    DISPUTE(100, "纠纷处理中"),
    CANCELED(110, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ServiceOrderStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isCompleted(Integer status) {
        return COMPLETED.status.equals(status);
    }

    public static boolean isCanceled(Integer status) {
        return CANCELED.status.equals(status);
    }

    public static boolean isWorkerSubmitted(Integer status) {
        return WORKER_SUBMITTED.status.equals(status);
    }

    /**
     * 是否为可验收状态（打手已提交完成）
     */
    public static boolean isAcceptable(Integer status) {
        return WORKER_SUBMITTED.status.equals(status);
    }

    /**
     * 是否为可返工状态（打手已提交完成）
     */
    public static boolean isReworkable(Integer status) {
        return WORKER_SUBMITTED.status.equals(status);
    }

    /**
     * 是否有效订单状态（不包括已完成/已取消/售后/纠纷）
     */
    public static boolean isActiveOrder(Integer status) {
        return PENDING_DISPATCH.status.equals(status)
                || WAITING_DESIGNATED.status.equals(status)
                || POOL_PENDING.status.equals(status)
                || ACCEPTED_PENDING_START.status.equals(status)
                || IN_PROGRESS.status.equals(status)
                || WORKER_SUBMITTED.status.equals(status);
    }

    /**
     * 是否允许申请普通取消（服务尚未正式开始）
     */
    public static boolean canCancel(Integer status) {
        return PENDING_DISPATCH.status.equals(status)
                || POOL_PENDING.status.equals(status)
                || ACCEPTED_PENDING_START.status.equals(status);
    }

    /**
     * 是否允许申请售后（服务已开始或已完成）
     */
    public static boolean canAfterSale(Integer status) {
        return IN_PROGRESS.status.equals(status)
                || WORKER_SUBMITTED.status.equals(status)
                || PENDING_VERIFICATION.status.equals(status)
                || COMPLETED.status.equals(status);
    }

    /**
     * 是否售后/纠纷相关状态
     */
    public static boolean isAfterSaleOrDispute(Integer status) {
        return AFTER_SALE.status.equals(status) || DISPUTE.status.equals(status);
    }

}
