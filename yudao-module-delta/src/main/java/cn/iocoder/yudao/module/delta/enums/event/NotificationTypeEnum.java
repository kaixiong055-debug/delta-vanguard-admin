package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 站内通知类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {

    SYSTEM("SYSTEM", "系统通知"),
    ORDER("ORDER", "订单通知"),
    SETTLEMENT("SETTLEMENT", "结算通知"),
    REFUND("REFUND", "退款通知"),
    REMINDER("REMINDER", "提醒通知"),
    ;

    private final String type;
    private final String description;
}
