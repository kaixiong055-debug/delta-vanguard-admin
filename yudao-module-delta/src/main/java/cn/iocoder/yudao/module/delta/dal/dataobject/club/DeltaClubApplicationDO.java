package cn.iocoder.yudao.module.delta.dal.dataobject.club;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 俱乐部入驻申请 DO
 * <p>
 * 申请发生在租户创建前，故继承 BaseDO 而非 TenantBaseDO。
 *
 * @author Delta-Vanguard
 */
@TableName("delta_club_application")
@KeySequence("delta_club_application_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaClubApplicationDO extends BaseDO {

    @TableId
    private Long id;

    /** 申请编号 (DCA + yyyyMMddHHmmss + 自增) */
    private String applicationNo;

    /** 申请会员用户ID (关联 member_user.id) */
    private Long applicantMemberId;

    /** 俱乐部名称 */
    private String clubName;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人手机号 */
    private String contactMobile;

    /** 联系人微信 */
    private String contactWechat;

    /** 俱乐部描述 */
    private String description;

    /** Logo URL */
    private String logoUrl;

    /** 资质凭证图片URL列表 (JSON数组) */
    private String qualificationUrls;

    /** 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销 */
    private Integer applicationStatus;

    /** 拒绝原因 */
    private String rejectReason;

    /** 审核人ID (关联 admin_user.id) */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审批通过后关联的租户ID */
    private Long approvedTenantId;

    /** 审核备注 */
    private String remark;

    /** 乐观锁版本号 */
    private Integer version;

}
