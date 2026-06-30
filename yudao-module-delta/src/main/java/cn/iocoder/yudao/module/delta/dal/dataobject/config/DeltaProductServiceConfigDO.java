package cn.iocoder.yudao.module.delta.dal.dataobject.config;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品服务配置 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_product_service_config")
@KeySequence("delta_product_service_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaProductServiceConfigDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 商品SPU ID（关联 product_spu.id）
     */
    private Long spuId;
    /**
     * 商品SKU ID（关联 product_sku.id）
     */
    private Long skuId;
    /**
     * 服务类型：1-陪玩 2-护航 3-趣味单
     */
    private Integer serviceType;
    /**
     * 设备类型：1-手机 2-平板 3-PC
     */
    private Integer deviceType;
    /**
     * 要求打手等级：1-初级 2-中级 3-高级 4-资深
     */
    private Integer requiredWorkerLevel;
    /**
     * 是否允许指定打手：0-否 1-是
     */
    private Boolean allowDesignatedWorker;
    /**
     * 是否允许大厅抢单：0-否 1-是
     */
    private Boolean allowPublicClaim;
    /**
     * 默认派单方式：1-客户指定 2-客服派单 3-接单大厅
     */
    private Integer defaultDispatchMode;
    /**
     * 最大服务时长（小时）
     */
    private Integer maxServiceHours;
    /**
     * 抽成比例（万分制，如1500表示15.00%）
     */
    private Integer commissionRate;
    /**
     * 是否启用：0-禁用 1-启用
     */
    private Boolean enabled;

}
