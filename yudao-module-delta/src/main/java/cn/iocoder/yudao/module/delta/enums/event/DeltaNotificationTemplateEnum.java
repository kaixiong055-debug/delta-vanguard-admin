package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Delta 通知模板枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaNotificationTemplateEnum {

    // ========== 派单与接单 ==========
    ORDER_DISPATCHED("ORDER_DISPATCHED", "订单已派给你",
            "订单 {orderNo} 已指派给你，请尽快处理",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    ORDER_CLAIMED("ORDER_CLAIMED", "你已成功接单",
            "你已成功接单 {orderNo}，请尽快开始服务",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    ORDER_REASSIGNED("ORDER_REASSIGNED", "订单已改派",
            "订单 {orderNo} 已改派给其他打手",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    ORDER_ENTERED_POOL("ORDER_ENTERED_POOL", "新订单已进入订单池",
            "订单 {orderNo} 已进入订单池，快去抢单吧",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    // ========== 履约 ==========
    SERVICE_STARTED("SERVICE_STARTED", "打手已开始服务",
            "打手已开始处理你的订单 {orderNo}",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.BUYER),

    SERVICE_SUBMITTED("SERVICE_SUBMITTED", "打手已提交完成",
            "打手已完成订单 {orderNo}，请前往验收",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.BUYER),

    SERVICE_REWORK_REQUESTED("SERVICE_REWORK_REQUESTED", "老板要求返工",
            "老板要求对订单 {orderNo} 进行返工，原因：{reason}",
            new String[]{"orderNo", "reason"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    SERVICE_ACCEPTED("SERVICE_ACCEPTED", "服务已验收完成",
            "你的订单 {orderNo} 已验收完成",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.WORKER),

    // ========== 结算 ==========
    SETTLEMENT_APPROVED("SETTLEMENT_APPROVED", "结算审核通过",
            "你的结算单 {settlementNo}（订单 {orderNo}）已审核通过，金额 {amount}",
            new String[]{"settlementNo", "orderNo", "amount"}, NotificationTypeEnum.SETTLEMENT, RecipientTypeEnum.WORKER),

    SETTLEMENT_REJECTED("SETTLEMENT_REJECTED", "结算审核驳回",
            "你的结算单 {settlementNo}（订单 {orderNo}）已被驳回，原因：{reason}",
            new String[]{"settlementNo", "orderNo", "reason"}, NotificationTypeEnum.SETTLEMENT, RecipientTypeEnum.WORKER),

    SETTLEMENT_PAID("SETTLEMENT_PAID", "结算已打款",
            "你的结算单 {settlementNo} 已打款，金额 {amount}",
            new String[]{"settlementNo", "amount"}, NotificationTypeEnum.SETTLEMENT, RecipientTypeEnum.WORKER),

    // ========== 取消与售后 ==========
    CANCEL_APPROVED("CANCEL_APPROVED", "取消申请已通过",
            "你的订单 {orderNo} 取消申请已通过",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.BUYER),

    CANCEL_REJECTED("CANCEL_REJECTED", "取消申请已驳回",
            "你的订单 {orderNo} 取消申请已被驳回",
            new String[]{"orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.BUYER),

    AFTER_SALE_ARBITRATED("AFTER_SALE_ARBITRATED", "售后仲裁已完成",
            "你的售后案件 {afterSaleNo}（订单 {orderNo}）仲裁已完成",
            new String[]{"afterSaleNo", "orderNo"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.BUYER),

    // ========== 退款 ==========
    REFUND_COMPLETED("REFUND_COMPLETED", "退款已人工完成",
            "你的退款 {refundNo}（订单 {orderNo}）已完成，金额 {amount}",
            new String[]{"refundNo", "orderNo", "amount"}, NotificationTypeEnum.REFUND, RecipientTypeEnum.BUYER),

    REFUND_FAILED("REFUND_FAILED", "退款处理失败",
            "退款 {refundNo}（订单 {orderNo}）处理失败，请联系客服",
            new String[]{"refundNo", "orderNo"}, NotificationTypeEnum.REFUND, RecipientTypeEnum.BUYER),

    // ========== 追回 ==========
    RECOVERY_NEEDED("RECOVERY_NEEDED", "追回任务需要处理",
            "追回任务 {recoveryNo}（订单 {orderNo}）需要处理，金额 {amount}",
            new String[]{"recoveryNo", "orderNo", "amount"}, NotificationTypeEnum.ORDER, RecipientTypeEnum.ADMIN),

    // ========== 超时提醒 ==========
    REMINDER_DISPATCH_PENDING("REMINDER_DISPATCH_PENDING", "待派单超时提醒",
            "订单 {orderNo} 在订单池中已超过 {hours} 小时未派单",
            new String[]{"orderNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.ADMIN),

    REMINDER_WORKER_START("REMINDER_WORKER_START", "打手待开始提醒",
            "订单 {orderNo} 已接单超过 {hours} 小时未开始服务",
            new String[]{"orderNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.WORKER),

    REMINDER_WORKER_START_ADMIN("REMINDER_WORKER_START_ADMIN", "打手待开始提醒",
            "订单 {orderNo} 已接单超过 {hours} 小时未开始服务",
            new String[]{"orderNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.ADMIN),

    REMINDER_PROGRESS_SILENT("REMINDER_PROGRESS_SILENT", "服务无进度提醒",
            "订单 {orderNo} 已超过 {hours} 小时无进度更新",
            new String[]{"orderNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.WORKER),

    REMINDER_ACCEPTANCE_PENDING("REMINDER_ACCEPTANCE_PENDING", "待验收提醒",
            "订单 {orderNo} 已提交完成超过 {hours} 小时未验收",
            new String[]{"orderNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.BUYER),

    REMINDER_REFUND_PENDING("REMINDER_REFUND_PENDING", "退款待处理提醒",
            "退款 {refundNo} 已超过 {hours} 小时未处理",
            new String[]{"refundNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.ADMIN),

    REMINDER_RECOVERY_PENDING("REMINDER_RECOVERY_PENDING", "追回待处理提醒",
            "追回任务 {recoveryNo} 已超过 {hours} 小时未处理",
            new String[]{"recoveryNo", "hours"}, NotificationTypeEnum.REMINDER, RecipientTypeEnum.ADMIN),
    ;

    private final String code;
    private final String title;
    private final String template;
    private final String[] paramNames;
    private final NotificationTypeEnum notificationType;
    private final RecipientTypeEnum recipientType;

    private static final Map<String, DeltaNotificationTemplateEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(DeltaNotificationTemplateEnum::getCode, e -> e));

    public static DeltaNotificationTemplateEnum fromCode(String code) {
        return CODE_MAP.get(code);
    }

    /**
     * 渲染模板内容
     */
    public String render(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        String result = template;
        for (String paramName : paramNames) {
            String value = params.getOrDefault(paramName, "");
            result = result.replace("{" + paramName + "}", value);
        }
        // 限制长度500字符
        if (result.length() > 500) {
            result = result.substring(0, 497) + "...";
        }
        return result;
    }

    /**
     * 校验参数是否齐全
     */
    public boolean validateParams(Map<String, String> params) {
        if (params == null) {
            return paramNames.length == 0;
        }
        for (String paramName : paramNames) {
            if (!params.containsKey(paramName)) {
                return false;
            }
        }
        return true;
    }
}
