package cn.iocoder.yudao.module.delta.service.worker;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerSkillMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.WORKER_SKILL_DUPLICATE;

/**
 * 打手技能 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaWorkerSkillServiceImpl implements DeltaWorkerSkillService {

    @Resource
    private DeltaWorkerSkillMapper deltaWorkerSkillMapper;

    @Override
    public List<DeltaWorkerSkillDO> getSkillListByWorkerId(Long workerId) {
        return deltaWorkerSkillMapper.selectListByWorkerId(workerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceSkills(Long workerId, List<DeltaWorkerSkillDO> skills) {
        // 先校验无重复
        if (CollUtil.isNotEmpty(skills)) {
            validateSkillsNoDuplication(skills);
        }
        // 逻辑删除旧技能
        deltaWorkerSkillMapper.delete(DeltaWorkerSkillDO::getWorkerId, workerId);
        // 插入新技能
        if (CollUtil.isNotEmpty(skills)) {
            for (DeltaWorkerSkillDO skill : skills) {
                skill.setWorkerId(workerId);
                deltaWorkerSkillMapper.insert(skill);
            }
        }
    }

    @Override
    public void validateSkillsNoDuplication(List<DeltaWorkerSkillDO> skills) {
        Set<String> keySet = new HashSet<>();
        for (DeltaWorkerSkillDO skill : skills) {
            String key = skill.getDeviceType() + "_" + skill.getServiceType();
            if (!keySet.add(key)) {
                throw exception(WORKER_SKILL_DUPLICATE);
            }
        }
    }

}
