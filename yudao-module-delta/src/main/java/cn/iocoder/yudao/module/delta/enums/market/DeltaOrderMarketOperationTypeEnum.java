package cn.iocoder.yudao.module.delta.enums.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台订单市场操作类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum DeltaOrderMarketOperationTypeEnum {

    PUBLISH("PUBLISH", "发布挂牌"),
    CLAIM("CLAIM", "俱乐部抢单"),
    ASSIGN("ASSIGN", "平台指定俱乐部"),
    WITHDRAW("WITHDRAW", "撤回挂牌"),
    EXPIRE("EXPIRE", "过期关闭"),
    CLOSE("CLOSE", "平台关闭"),
    CLAIM_FAILED("CLAIM_FAILED", "抢单失败");

    private final String type;
    private final String description;

    private static final Map<String, DeltaOrderMarketOperationTypeEnum> TYPE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(DeltaOrderMarketOperationTypeEnum::getType, e -> e));

    public static DeltaOrderMarketOperationTypeEnum fromType(String type) {
        return TYPE_MAP.get(type);
    }
}
