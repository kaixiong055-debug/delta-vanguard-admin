package cn.iocoder.yudao.module.delta.service.worker;

import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;

import java.util.List;

/**
 * 打手技能 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaWorkerSkillService {

    /**
     * 根据打手ID查询技能列表
     *
     * @param workerId 打手ID
     * @return 技能列表
     */
    List<DeltaWorkerSkillDO> getSkillListByWorkerId(Long workerId);

    /**
     * 整体替换打手技能（事务）
     * 先删除旧技能（逻辑删除），再批量插入新技能
     *
     * @param workerId 打手ID
     * @param skills   新技能列表（空列表表示清空）
     */
    void replaceSkills(Long workerId, List<DeltaWorkerSkillDO> skills);

    /**
     * 校验技能列表无重复项
     *
     * @param skills 技能列表
     */
    void validateSkillsNoDuplication(List<DeltaWorkerSkillDO> skills);

}
