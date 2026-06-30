package cn.iocoder.yudao.module.delta.dal.dataobject.club;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 俱乐部档案 DO
 * <p>
 * 与 system_tenant 一一对应，tenant_id 唯一约束。
 *
 * @author Delta-Vanguard
 */
@TableName("delta_club_profile")
@KeySequence("delta_club_profile_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaClubProfileDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 俱乐部编码（系统唯一） */
    private String clubCode;

    /** 俱乐部名称 */
    private String clubName;

    /** 俱乐部所有者会员用户ID (关联 member_user.id) */
    private Long ownerMemberId;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人手机号 */
    private String contactMobile;

    /** 联系人微信 */
    private String contactWechat;

    /** Logo URL */
    private String logoUrl;

    /** 俱乐部描述 */
    private String description;

    /** 经营状态：0-停用 1-启用 */
    private Integer businessStatus;

    /** 平台抽成比例（万分制，如 500 = 5.00%） */
    private Integer platformCommissionRate;

    /** 最大并发订单数 */
    private Integer maxConcurrentOrders;

    /** 关联的入驻申请ID */
    private Long applicationId;

    /** 备注 */
    private String remark;

    /** 乐观锁版本号 */
    private Integer version;

}
