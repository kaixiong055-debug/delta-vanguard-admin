package cn.iocoder.yudao.module.delta.service.worker;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerUpdateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerProfileUpdateReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 打手资料 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaWorkerServiceImpl implements DeltaWorkerService {

    @Resource
    private DeltaWorkerMapper deltaWorkerMapper;

    @Resource
    private DeltaWorkerSkillService deltaWorkerSkillService;

    @Override
    public DeltaWorkerDO getWorkerByUserId(Long userId) {
        return deltaWorkerMapper.selectByUserId(userId);
    }

    @Override
    public DeltaWorkerDO getWorker(Long id) {
        return deltaWorkerMapper.selectById(id);
    }

    @Override
    public DeltaWorkerDO validateWorkerAvailable(Long id) {
        DeltaWorkerDO worker = deltaWorkerMapper.selectById(id);
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        if (!WorkerAuditStatusEnum.isApproved(worker.getAuditStatus())) {
            throw exception(WORKER_NOT_APPROVED);
        }
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            throw exception(WORKER_DISABLED);
        }
        return worker;
    }

    @Override
    public PageResult<DeltaWorkerDO> getWorkerPage(DeltaWorkerPageReqVO pageReqVO) {
        return deltaWorkerMapper.selectPage(pageReqVO);
    }

    @Override
    public DeltaWorkerDO getWorkerIdentity(Long userId) {
        return deltaWorkerMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyProfile(Long userId, AppDeltaWorkerProfileUpdateReqVO reqVO) {
        DeltaWorkerDO worker = deltaWorkerMapper.selectByUserId(userId);
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        // 白名单字段
        if (reqVO.getDisplayName() != null) {
            worker.setDisplayName(reqVO.getDisplayName());
        }
        if (reqVO.getAvatar() != null) {
            worker.setAvatar(reqVO.getAvatar());
        }
        if (reqVO.getPhone() != null) {
            worker.setPhone(reqVO.getPhone());
        }
        deltaWorkerMapper.updateById(worker);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyWorkStatus(Long userId, Integer workStatus) {
        // 校验状态值有效性
        WorkerWorkStatusEnum statusEnum = WorkerWorkStatusEnum.valueOf(workStatus);
        if (statusEnum == null) {
            throw exception(WORKER_WORK_STATUS_INVALID);
        }
        // 不允许主动设为忙碌
        if (WorkerWorkStatusEnum.BUSY.getStatus().equals(workStatus)) {
            throw exception(WORKER_WORK_STATUS_INVALID);
        }
        // 获取当前的打手资料
        DeltaWorkerDO worker = deltaWorkerMapper.selectByUserId(userId);
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        // 切换在线需满足条件
        if (WorkerWorkStatusEnum.ONLINE.getStatus().equals(workStatus)) {
            validateCanGoOnline(worker);
        }
        worker.setWorkStatus(workStatus);
        deltaWorkerMapper.updateById(worker);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorker(DeltaWorkerUpdateReqVO reqVO) {
        DeltaWorkerDO worker = deltaWorkerMapper.selectById(reqVO.getId());
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        // 白名单字段
        if (reqVO.getDisplayName() != null) {
            worker.setDisplayName(reqVO.getDisplayName());
        }
        if (reqVO.getRealName() != null) {
            worker.setRealName(reqVO.getRealName());
        }
        if (reqVO.getPhone() != null) {
            worker.setPhone(reqVO.getPhone());
        }
        if (reqVO.getAvatar() != null) {
            worker.setAvatar(reqVO.getAvatar());
        }
        if (reqVO.getLevel() != null) {
            worker.setLevel(reqVO.getLevel());
        }
        if (reqVO.getCommissionRate() != null) {
            worker.setCommissionRate(reqVO.getCommissionRate());
        }
        if (reqVO.getMaxOrderCount() != null) {
            worker.setMaxOrderCount(reqVO.getMaxOrderCount());
        }
        if (reqVO.getIsRecommend() != null) {
            worker.setIsRecommend(reqVO.getIsRecommend());
        }
        if (reqVO.getAuditRemark() != null) {
            worker.setAuditRemark(reqVO.getAuditRemark());
        }
        deltaWorkerMapper.updateById(worker);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkerStatus(Long id, Integer status, String reason) {
        DeltaWorkerDO worker = deltaWorkerMapper.selectById(id);
        if (worker == null) {
            throw exception(WORKER_NOT_EXISTS);
        }
        worker.setStatus(status);
        // 停用时同步设置离线
        if (!CommonStatusEnum.isEnable(status)) {
            worker.setWorkStatus(WorkerWorkStatusEnum.OFFLINE.getStatus());
        }
        if (reason != null) {
            worker.setAuditRemark(reason);
        }
        deltaWorkerMapper.updateById(worker);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorker(DeltaWorkerDO worker) {
        deltaWorkerMapper.insert(worker);
        return worker.getId();
    }

    /**
     * 校验打手是否可以上线
     */
    private void validateCanGoOnline(DeltaWorkerDO worker) {
        // 必须审核通过
        if (!WorkerAuditStatusEnum.isApproved(worker.getAuditStatus())) {
            throw exception(WORKER_CANNOT_GO_ONLINE);
        }
        // 必须启用
        if (!CommonStatusEnum.isEnable(worker.getStatus())) {
            throw exception(WORKER_CANNOT_GO_ONLINE);
        }
        // 必须至少有一条启用技能
        List<DeltaWorkerSkillDO> skills = deltaWorkerSkillService.getSkillListByWorkerId(worker.getId());
        if (skills == null || skills.isEmpty()) {
            throw exception(WORKER_CANNOT_GO_ONLINE);
        }
        boolean hasEnabledSkill = skills.stream()
                .anyMatch(s -> CommonStatusEnum.isEnable(s.getStatus()));
        if (!hasEnabledSkill) {
            throw exception(WORKER_CANNOT_GO_ONLINE);
        }
    }

    @Override
    public DeltaWorkerMapper getWorkerMapper() {
        return deltaWorkerMapper;
    }

}
