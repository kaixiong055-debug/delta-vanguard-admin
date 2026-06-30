package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Delta 领域事件类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaEventTypeEnum {

    // ========== 打手与申请 ==========
    WORKER_APPLICATION_SUBMITTED("WORKER_APPLICATION_SUBMITTED", "打手申请已提交"),
    WORKER_APPLICATION_APPROVED("WORKER_APPLICATION_APPROVED", "打手申请已通过"),
    WORKER_APPLICATION_REJECTED("WORKER_APPLICATION_REJECTED", "打手申请已驳回"),
    WORKER_ENABLED("WORKER_ENABLED", "打手已启用"),
    WORKER_DISABLED("WORKER_DISABLED", "打手已禁用"),

    // ========== 服务单和派单 ==========
    SERVICE_ORDER_CREATED("SERVICE_ORDER_CREATED", "服务订单已创建"),
    SERVICE_ORDER_CONFIRMED("SERVICE_ORDER_CONFIRMED", "服务订单已确认"),
    SERVICE_ORDER_ENTERED_POOL("SERVICE_ORDER_ENTERED_POOL", "服务订单已进入订单池"),
    SERVICE_ORDER_DISPATCHED("SERVICE_ORDER_DISPATCHED", "服务订单已派单"),
    SERVICE_ORDER_REASSIGNED("SERVICE_ORDER_REASSIGNED", "服务订单已改派"),
    SERVICE_ORDER_CLAIMED("SERVICE_ORDER_CLAIMED", "打手已接单"),
    SERVICE_ORDER_RETURNED_POOL("SERVICE_ORDER_RETURNED_POOL", "服务订单已退回订单池"),

    // ========== 履约 ==========
    SERVICE_STARTED("SERVICE_STARTED", "打手已开始服务"),
    SERVICE_PROGRESS_UPDATED("SERVICE_PROGRESS_UPDATED", "服务进度已更新"),
    SERVICE_EVIDENCE_ADDED("SERVICE_EVIDENCE_ADDED", "服务凭证已添加"),
    SERVICE_COMPLETION_SUBMITTED("SERVICE_COMPLETION_SUBMITTED", "打手已提交完成"),
    SERVICE_REWORK_REQUESTED("SERVICE_REWORK_REQUESTED", "老板要求返工"),
    SERVICE_ACCEPTED("SERVICE_ACCEPTED", "服务已验收完成"),

    // ========== 结算 ==========
    SETTLEMENT_CREATED("SETTLEMENT_CREATED", "结算单已生成"),
    SETTLEMENT_APPROVED("SETTLEMENT_APPROVED", "结算审核已通过"),
    SETTLEMENT_REJECTED("SETTLEMENT_REJECTED", "结算审核已驳回"),
    SETTLEMENT_PAID("SETTLEMENT_PAID", "结算已打款"),

    // ========== 取消和售后 ==========
    CANCEL_APPLIED("CANCEL_APPLIED", "取消申请已提交"),
    CANCEL_APPROVED("CANCEL_APPROVED", "取消申请已通过"),
    CANCEL_REJECTED("CANCEL_REJECTED", "取消申请已驳回"),
    AFTER_SALE_APPLIED("AFTER_SALE_APPLIED", "售后案件已提交"),
    AFTER_SALE_ACCEPTED("AFTER_SALE_ACCEPTED", "售后案件已受理"),
    AFTER_SALE_REJECTED("AFTER_SALE_REJECTED", "售后案件已驳回"),
    AFTER_SALE_ARBITRATED("AFTER_SALE_ARBITRATED", "售后仲裁已完成"),

    // ========== 退款和追回 ==========
    REFUND_CREATED("REFUND_CREATED", "退款记录已创建"),
    REFUND_PROCESSING("REFUND_PROCESSING", "退款处理中"),
    REFUND_COMPLETED("REFUND_COMPLETED", "退款已完成"),
    REFUND_FAILED("REFUND_FAILED", "退款处理失败"),
    RECOVERY_CREATED("RECOVERY_CREATED", "追回任务已生成"),
    RECOVERY_PARTIAL("RECOVERY_PARTIAL", "追回部分完成"),
    RECOVERY_COMPLETED("RECOVERY_COMPLETED", "追回任务已完成"),
    RECOVERY_FAILED("RECOVERY_FAILED", "追回任务失败"),

    // ========== 超时提醒 ==========
    DISPATCH_PENDING_TIMEOUT("DISPATCH_PENDING_TIMEOUT", "待派单超时提醒"),
    WORKER_START_TIMEOUT("WORKER_START_TIMEOUT", "打手待开始超时提醒"),
    PROGRESS_SILENT_TIMEOUT("PROGRESS_SILENT_TIMEOUT", "服务无进度超时提醒"),
    ACCEPTANCE_PENDING_TIMEOUT("ACCEPTANCE_PENDING_TIMEOUT", "待验收超时提醒"),
    REFUND_PENDING_TIMEOUT("REFUND_PENDING_TIMEOUT", "退款待处理超时提醒"),
    RECOVERY_PENDING_TIMEOUT("RECOVERY_PENDING_TIMEOUT", "追回待处理超时提醒"),

    // ========== Phase 9 俱乐部 ==========
    CLUB_APPLICATION_SUBMITTED("CLUB_APPLICATION_SUBMITTED", "俱乐部入驻申请已提交"),
    CLUB_APPLICATION_APPROVED("CLUB_APPLICATION_APPROVED", "俱乐部入驻申请已通过"),
    CLUB_APPLICATION_REJECTED("CLUB_APPLICATION_REJECTED", "俱乐部入驻申请已驳回"),
    CLUB_APPLICATION_CANCELED("CLUB_APPLICATION_CANCELED", "俱乐部入驻申请已撤销"),
    CLUB_CREATED("CLUB_CREATED", "俱乐部已创建"),
    CLUB_STATUS_CHANGED("CLUB_STATUS_CHANGED", "俱乐部经营状态已变更"),
    CLUB_SERVICE_SCOPE_CHANGED("CLUB_SERVICE_SCOPE_CHANGED", "俱乐部服务范围已变更"),

    // ========== Phase 10 订单市场 ==========
    ORDER_MARKET_PUBLISHED("ORDER_MARKET_PUBLISHED", "订单已挂牌到市场"),
    ORDER_MARKET_CLAIMED("ORDER_MARKET_CLAIMED", "俱乐部已抢单"),
    ORDER_MARKET_ASSIGNED("ORDER_MARKET_ASSIGNED", "平台已指定俱乐部接单"),
    ORDER_MARKET_WITHDRAWN("ORDER_MARKET_WITHDRAWN", "挂牌已撤回"),
    ORDER_MARKET_EXPIRED("ORDER_MARKET_EXPIRED", "挂牌已过期"),
    ;

    private final String type;
    private final String description;

    private static final Map<String, DeltaEventTypeEnum> TYPE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(DeltaEventTypeEnum::getType, e -> e));

    public static DeltaEventTypeEnum fromType(String type) {
        return TYPE_MAP.get(type);
    }

    public static boolean isValid(String type) {
        return TYPE_MAP.containsKey(type);
    }
}
