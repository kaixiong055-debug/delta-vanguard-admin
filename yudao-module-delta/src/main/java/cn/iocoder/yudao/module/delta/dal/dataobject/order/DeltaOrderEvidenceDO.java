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
 * 完成凭证 DO
 *
 * @author Delta-Vanguard
 */
@TableName(value = "delta_order_evidence", autoResultMap = true)
@KeySequence("delta_order_evidence_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaOrderEvidenceDO extends TenantBaseDO {

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
     * 凭证类型：1-截图 2-视频 3-文字说明 4-文件
     */
    private Integer evidenceType;
    /**
     * 凭证内容/说明
     */
    private String content;
    /**
     * 凭证图片URL列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;
    /**
     * 凭证视频URL
     */
    private String videoUrl;
    /**
     * 审核状态：0-待审核 1-审核通过 2-审核驳回
     */
    private Integer reviewStatus;
    /**
     * 审核备注
     */
    private String reviewRemark;

}
