package cn.iocoder.yudao.module.delta.dal.dataobject.worker;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 打手技能 DO
 *
 * @author Delta-Vanguard
 */
@TableName("delta_worker_skill")
@KeySequence("delta_worker_skill_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerSkillDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 打手ID（关联 delta_worker.id）
     */
    private Long workerId;
    /**
     * 设备类型：1-手机 2-平板 3-PC
     */
    private Integer deviceType;
    /**
     * 服务类型：1-陪玩 2-护航 3-趣味单
     */
    private Integer serviceType;
    /**
     * 技能等级：1-初级 2-中级 3-高级 4-资深
     */
    private Integer skillLevel;
    /**
     * 状态：0-开启 1-关闭（对应 CommonStatusEnum）
     */
    private Integer status;

}
