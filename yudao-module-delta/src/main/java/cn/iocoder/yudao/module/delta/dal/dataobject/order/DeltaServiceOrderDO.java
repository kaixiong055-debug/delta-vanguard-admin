package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 服务履约订单 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_service_order")
@KeySequence("delta_service_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaServiceOrderDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 服务订单号
     */
    private String serviceOrderNo;
    /**
     * 商城订单ID（关联 trade_order.id）
     */
    private Long tradeOrderId;
    /**
     * 商城订单号
     */
    private String tradeOrderNo;
    /**
     * 商城订单项ID（关联 trade_order_item.id）
     */
    private Long tradeOrderItemId;
    /**
     * 买家会员用户ID
     */
    private Long buyerUserId;
    /**
     * 商品SPU ID
     */
    private Long spuId;
    /**
     * 商品SKU ID
     */
    private Long skuId;
    /**
     * 商品名称（快照）
     */
    private String productName;
    /**
     * SKU名称（快照）
     */
    private String skuName;
    /**
     * 商品图片（快照）
     */
    private String productPicUrl;
    /**
     * 购买数量（快照）
     */
    private Integer count;
    /**
     * 服务类型：1-陪玩 2-护航 3-趣味单
     */
    private Integer serviceType;
    /**
     * 设备类型：1-手机 2-平板 3-PC
     */
    private Integer deviceType;
    /**
     * 服务金额（分，快照）
     */
    private Integer serviceAmount;
    /**
     * 派单方式：1-客户指定 2-客服派单 3-接单大厅
     */
    private Integer dispatchMode;
    /**
     * 客户指定打手ID
     */
    private Long preferredWorkerId;
    /**
     * 最终指派打手ID
     */
    private Long assignedWorkerId;
    /**
     * 服务状态：10-待派单 20-等待指定打手确认 30-接单大厅待领取 40-已接单待开始 50-服务进行中 60-打手已提交完成 70-待验收 80-已完成 90-售后处理中 100-纠纷处理中 110-已取消
     */
    private Integer status;
    /**
     * 乐观锁版本号
     */
    private Integer version;
    /**
     * 客户备注
     */
    private String customerRemark;
    /**
     * 后台备注
     */
    private String adminRemark;
    /**
     * 大厅接单截止时间
     */
    private LocalDateTime claimDeadline;
    /**
     * 接单时间
     */
    private LocalDateTime acceptedAt;
    /**
     * 服务开始时间
     */
    private LocalDateTime startedAt;
    /**
     * 提交完成时间
     */
    private LocalDateTime submittedAt;
    /**
     * 验收时间
     */
    private LocalDateTime verifiedAt;
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    /**
     * 取消原因
     */
    private String cancelReason;
    /**
     * 抽成比例（万分制，快照，如1500表示15.00%）
     */
    private Integer commissionRate;
    /**
     * 平台抽成金额（分）
     */
    private Integer platformFee;
    /**
     * 打手收入金额（分）
     */
    private Integer workerAmount;

}
