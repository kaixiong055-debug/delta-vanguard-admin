package cn.iocoder.yudao.module.delta.enums.settlement;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算操作类型枚举
 *
 * @author Delta-Vanguard
 */
@Getter
@AllArgsConstructor
public enum SettlementOperationTypeEnum {

    /** 生成结算 */
    GENERATE("GENERATE", "生成结算"),
    /** 审核通过 */
    APPROVE("APPROVE", "审核通过"),
    /** 审核驳回 */
    REJECT("REJECT", "审核驳回"),
    /** 重新提交审核 */
    RESUBMIT("RESUBMIT", "重新提交审核"),
    /** 标记已打款 */
    MARK_PAID("MARK_PAID", "标记已打款"),
    /** 撤销打款 */
    REVOKE_PAID("REVOKE_PAID", "撤销打款");

    private final String type;
    private final String name;

}
