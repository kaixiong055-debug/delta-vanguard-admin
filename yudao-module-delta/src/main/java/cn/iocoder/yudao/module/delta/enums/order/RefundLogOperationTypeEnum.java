package cn.iocoder.yudao.module.delta.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 退款操作日志类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RefundLogOperationTypeEnum implements ArrayValuable<String> {

    CREATE("CREATE", "创建退款记录"),
    START("START", "开始处理"),
    COMPLETE("COMPLETE", "退款完成"),
    FAIL("FAIL", "退款失败"),
    RETRY("RETRY", "重新处理"),
    CANCEL("CANCEL", "撤销退款");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(RefundLogOperationTypeEnum::getType).toArray(String[]::new);

    private final String type;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
