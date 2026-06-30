package cn.iocoder.yudao.module.delta.dal.mysql.worker;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 打手技能 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerSkillMapper extends BaseMapperX<DeltaWorkerSkillDO> {

    default List<DeltaWorkerSkillDO> selectListByWorkerId(Long workerId) {
        return selectList(DeltaWorkerSkillDO::getWorkerId, workerId);
    }

    /**
     * 查询打手启用的技能列表
     */
    default List<DeltaWorkerSkillDO> selectEnabledListByWorkerId(Long workerId) {
        return selectList(new LambdaQueryWrapper<DeltaWorkerSkillDO>()
                .eq(DeltaWorkerSkillDO::getWorkerId, workerId)
                .eq(DeltaWorkerSkillDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }

    /**
     * 查询打手是否拥有匹配的启用技能
     */
    default boolean hasMatchingSkill(Long workerId, Integer deviceType, Integer serviceType) {
        return selectCount(new LambdaQueryWrapper<DeltaWorkerSkillDO>()
                .eq(DeltaWorkerSkillDO::getWorkerId, workerId)
                .eq(DeltaWorkerSkillDO::getDeviceType, deviceType)
                .eq(DeltaWorkerSkillDO::getServiceType, serviceType)
                .eq(DeltaWorkerSkillDO::getStatus, CommonStatusEnum.ENABLE.getStatus())) > 0;
    }

}
