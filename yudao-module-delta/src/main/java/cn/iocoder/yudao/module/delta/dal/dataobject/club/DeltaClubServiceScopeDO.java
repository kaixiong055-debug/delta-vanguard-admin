package cn.iocoder.yudao.module.delta.dal.dataobject.club;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 俱乐部服务范围 DO
 * <p>
 * serviceType 复用 ServiceTypeEnum 的真实值（1-陪玩 2-护航 3-趣味单）。
 *
 * @author Delta-Vanguard
 */
@TableName("delta_club_service_scope")
@KeySequence("delta_club_service_scope_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaClubServiceScopeDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 俱乐部档案ID (关联 delta_club_profile.id) */
    private Long clubProfileId;

    /** 服务类型：1-陪玩 2-护航 3-趣味单 */
    private Integer serviceType;

    /** 是否启用：0-禁用 1-启用 */
    private Boolean enabled;

    /** 备注 */
    private String remark;

}
