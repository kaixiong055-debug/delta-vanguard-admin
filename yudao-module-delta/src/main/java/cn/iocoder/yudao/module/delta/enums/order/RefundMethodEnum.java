package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 人工退款方式枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RefundMethodEnum implements ArrayValuable<Integer> {

    WECHAT(1, "人工微信"),
    BANK_CARD(2, "银行卡"),
    ALIPAY(3, "支付宝"),
    OTHER(4, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(RefundMethodEnum::getMethod).toArray(Integer[]::new);

    private final Integer method;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isValid(Integer method) {
        return Arrays.stream(values()).anyMatch(e -> e.getMethod().equals(method));
    }

}
