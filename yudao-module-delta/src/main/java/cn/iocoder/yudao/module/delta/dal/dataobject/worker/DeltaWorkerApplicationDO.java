package cn.iocoder.yudao.module.delta.dal.dataobject.worker;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打手申请 DO
 *
 * @author Delta-Vanguard
 */
@TableName(value = "delta_worker_application", autoResultMap = true)
@KeySequence("delta_worker_application_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerApplicationDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 会员用户ID
     */
    private Long userId;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 游戏账号UID
     */
    private String gameUid;
    /**
     * 设备类型：1-手机 2-平板 3-PC
     */
    private Integer deviceType;
    /**
     * 个人介绍
     */
    private String introduction;
    /**
     * 打手经验描述
     */
    private String experience;
    /**
     * 凭证图片URL列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceUrls;
    /**
     * 审核凭证图片URL
     */
    private String checkEvidenceUrl;
    /**
     * 申请状态：0-未申请 1-审核中 2-审核通过 3-审核驳回（复用 WorkerAuditStatusEnum 值）
     */
    private Integer applicationStatus;
    /**
     * 驳回原因
     */
    private String rejectReason;
    /**
     * 审核人ID（关联 admin_user.id）
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

}
