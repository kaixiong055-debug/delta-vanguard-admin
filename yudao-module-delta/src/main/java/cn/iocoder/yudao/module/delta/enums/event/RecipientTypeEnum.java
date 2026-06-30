package cn.iocoder.yudao.module.delta.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知接收人类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum RecipientTypeEnum {

    BUYER("BUYER", "老板/买家"),
    WORKER("WORKER", "打手"),
    ADMIN("ADMIN", "管理员"),
    SYSTEM("SYSTEM", "系统"),
    ;

    private final String type;
    private final String description;
}
