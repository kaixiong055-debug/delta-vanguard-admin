package cn.iocoder.yudao.module.delta.dal.dataobject.market;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 平台订单市场挂牌 DO
 * <p>
 * 订单市场属于平台级资源，继承 BaseDO（不继承 TenantBaseDO）
 *
 * @author Delta-Vanguard
 */
@TableName("delta_order_market_listing")
@KeySequence("delta_order_market_listing_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderMarketListingDO extends BaseDO {

    @TableId
    private Long id;

    /** 挂牌编号 */
    private String listingNo;

    /** 服务订单ID */
    private Long serviceOrderId;

    /** 服务订单号 */
    private String serviceOrderNo;

    /** 订单来源租户ID */
    private Long sourceTenantId;

    /** 服务类型：1-陪玩 2-护航 3-趣味单 */
    private Integer serviceType;

    /** 服务金额（单位：分） */
    private Integer serviceAmount;

    /** 需求摘要（脱敏后） */
    private String requirementSummary;

    /** 挂牌状态：0-可抢 1-已被接单 2-已撤回 3-已过期 4-已关闭 */
    private Integer listingStatus;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 接单俱乐部档案ID */
    private Long claimedClubId;

    /** 接单俱乐部租户ID */
    private Long claimedClubTenantId;

    /** 接单时间 */
    private LocalDateTime claimTime;

    /** 发布人ID */
    private Long publisherId;

    /** 撤回原因 */
    private String withdrawReason;

    /** 备注 */
    private String remark;

    /** 活动标记：1-有效 0-无效，与 service_order_id 组成唯一约束 */
    private Integer activeFlag;

    /** 乐观锁版本号 */
    private Integer version;
}
