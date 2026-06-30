package cn.iocoder.yudao.module.delta.dal.dataobject.market;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 市场操作日志 DO
 * <p>
 * 平台级资源，继承 BaseDO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_market_log")
@KeySequence("delta_order_market_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderMarketLogDO extends BaseDO {

    @TableId
    private Long id;

    /** 挂牌ID */
    private Long listingId;

    /** 服务订单ID */
    private Long serviceOrderId;

    /** 操作类型 */
    private String operationType;

    /** 操作方类型：PLATFORM/CLUB/SYSTEM */
    private String operatorType;

    /** 操作人ID */
    private Long operatorId;

    /** 操作俱乐部ID */
    private Long clubId;

    /** 操作俱乐部租户ID */
    private Long clubTenantId;

    /** 操作前状态 */
    private Integer beforeStatus;

    /** 操作后状态 */
    private Integer afterStatus;

    /** 是否成功：0-失败 1-成功 */
    private Integer success;

    /** 失败原因 */
    private String failureReason;

    /** 备注 */
    private String remark;
}
