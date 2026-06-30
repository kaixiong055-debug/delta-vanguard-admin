package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 售后案件 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_after_sale")
@KeySequence("delta_after_sale_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaAfterSaleDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 售后单号 */
    private String afterSaleNo;
    /** 服务订单ID */
    private Long serviceOrderId;
    /** 买家用户ID */
    private Long buyerUserId;
    /** 打手ID */
    private Long workerId;
    /** 售后类型 */
    private Integer afterSaleType;
    /** 原因类型 */
    private Integer reasonType;
    /** 申请原因 */
    private String reason;
    /** 问题描述 */
    private String description;
    /** 凭证图片URLs（JSON数组） */
    private String evidenceUrls;
    /** 售后状态：0-待处理 1-已受理 2-已驳回 3-已仲裁 4-已关闭 */
    private Integer status;
    /** 原始服务单状态（快照） */
    private Integer originalOrderStatus;
    /** 请求退款金额（分） */
    private Integer requestedRefundAmount;
    /** 批准退款金额（分） */
    private Integer approvedRefundAmount;
    /** 责任归属 */
    private Integer responsibilityType;
    /** 处理人ID */
    private Long handlerId;
    /** 处理时间 */
    private LocalDateTime handleTime;
    /** 处理备注 */
    private String handleRemark;
    /** 需要人工追回（已打款结算时标记） */
    private Boolean needManualRecovery;

}
