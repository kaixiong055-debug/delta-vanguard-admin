package cn.iocoder.yudao.module.delta.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 服务进度 DO
 *
 * @author Delta-Vanguard
 */
@TableName(value = "delta_order_progress", autoResultMap = true)
@KeySequence("delta_order_progress_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderProgressDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 服务订单ID
     */
    private Long serviceOrderId;
    /**
     * 打手ID
     */
    private Long workerId;
    /**
     * 进度类型：1-开始服务 2-进度更新 3-异常报告
     */
    private Integer progressType;
    /**
     * 进度百分比（0-100）
     */
    private Integer progressPercent;
    /**
     * 进度内容
     */
    private String content;
    /**
     * 进度图片URL列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;

}
