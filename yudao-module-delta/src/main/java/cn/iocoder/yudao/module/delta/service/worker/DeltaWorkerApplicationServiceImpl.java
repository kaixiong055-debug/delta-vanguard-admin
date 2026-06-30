package cn.iocoder.yudao.module.delta.service.worker;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationApproveReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.AppDeltaWorkerApplyReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerApplicationMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 打手申请 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaWorkerApplicationServiceImpl implements DeltaWorkerApplicationService {

    @Resource
    private DeltaWorkerApplicationMapper deltaWorkerApplicationMapper;

    @Resource
    private DeltaWorkerMapper deltaWorkerMapper;

    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;

    @Resource
    private DeltaWorkerSkillService deltaWorkerSkillService;

    // ========== App 端 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyWorker(Long userId, AppDeltaWorkerApplyReqVO reqVO) {
        // 检查是否已有有效打手身份
        DeltaWorkerDO existWorker = deltaWorkerMapper.selectByUserId(userId);
        if (existWorker != null && WorkerAuditStatusEnum.isApproved(existWorker.getAuditStatus())) {
            throw exception(WORKER_ALREADY_EXISTS);
        }
        // 检查是否已有审核中的申请
        DeltaWorkerApplicationDO pendingApp = deltaWorkerApplicationMapper.selectLatestByUserId(userId);
        if (pendingApp != null && WorkerAuditStatusEnum.isPending(pendingApp.getApplicationStatus())) {
            throw exception(WORKER_APPLICATION_ALREADY_PENDING);
        }
        // 创建申请
        DeltaWorkerApplicationDO application = new DeltaWorkerApplicationDO();
        application.setUserId(userId);
        application.setRealName(reqVO.getRealName());
        application.setPhone(reqVO.getPhone());
        application.setGameUid(reqVO.getGameUid());
        application.setDeviceType(reqVO.getDeviceType());
        application.setIntroduction(reqVO.getIntroduction());
        application.setExperience(reqVO.getExperience());
        application.setEvidenceUrls(reqVO.getEvidenceUrls());
        application.setCheckEvidenceUrl(reqVO.getCheckEvidenceUrl());
        application.setApplicationStatus(WorkerAuditStatusEnum.PENDING.getStatus());
        deltaWorkerApplicationMapper.insert(application);
        log.info("[applyWorker] 会员 user({}) 提交打手申请 application({})", userId, application.getId());
        return application.getId();
    }

    @Override
    public DeltaWorkerApplicationDO getLatestApplicationByUserId(Long userId) {
        return deltaWorkerApplicationMapper.selectLatestByUserId(userId);
    }

    @Override
    public List<DeltaWorkerApplicationDO> getApplicationListByUserId(Long userId) {
        return deltaWorkerApplicationMapper.selectListByUserId(userId);
    }

    // ========== Admin 端 ==========

    @Override
    public DeltaWorkerApplicationDO getApplication(Long id) {
        return deltaWorkerApplicationMapper.selectById(id);
    }

    @Override
    public PageResult<DeltaWorkerApplicationDO> getApplicationPage(DeltaWorkerApplicationPageReqVO pageReqVO) {
        return deltaWorkerApplicationMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(DeltaWorkerApplicationApproveReqVO reqVO, Long reviewerId) {
        // 1. 校验申请存在且为审核中
        DeltaWorkerApplicationDO application = validateApplicationPending(reqVO.getApplicationId());
        Long userId = application.getUserId();

        // 2. 检查是否已有有效打手（并发防线：数据库唯一约束  tenant_id+user_id）
        DeltaWorkerDO existWorker = deltaWorkerMapper.selectByUserId(userId);
        if (existWorker != null && WorkerAuditStatusEnum.isApproved(existWorker.getAuditStatus())) {
            throw exception(WORKER_ALREADY_EXISTS);
        }

        // 3. 创建打手资料
        DeltaWorkerDO worker = new DeltaWorkerDO();
        worker.setUserId(userId);
        worker.setWorkerNo(deltaNoRedisDAO.generateWorkerNo());
        worker.setRealName(application.getRealName());
        worker.setDisplayName(reqVO.getDisplayName());
        worker.setPhone(application.getPhone());
        worker.setLevel(reqVO.getLevel());
        worker.setCommissionRate(reqVO.getCommissionRate());
        worker.setMaxOrderCount(reqVO.getMaxOrderCount());
        worker.setIsRecommend(reqVO.getIsRecommend() != null ? reqVO.getIsRecommend() : false);
        worker.setAuditStatus(WorkerAuditStatusEnum.APPROVED.getStatus());
        worker.setWorkStatus(WorkerWorkStatusEnum.OFFLINE.getStatus());
        worker.setStatus(CommonStatusEnum.ENABLE.getStatus());
        worker.setAuditRemark(reqVO.getAuditRemark());
        worker.setApprovedAt(LocalDateTime.now());
        deltaWorkerMapper.insert(worker);
        log.info("[approveApplication] 创建打手 worker({}) for user({})", worker.getId(), userId);

        // 4. 创建技能
        if (CollUtil.isNotEmpty(reqVO.getSkills())) {
            List<DeltaWorkerSkillDO> skills = new ArrayList<>();
            for (DeltaWorkerApplicationApproveReqVO.SkillItem item : reqVO.getSkills()) {
                DeltaWorkerSkillDO skill = new DeltaWorkerSkillDO();
                skill.setWorkerId(worker.getId());
                skill.setDeviceType(item.getDeviceType());
                skill.setServiceType(item.getServiceType());
                skill.setSkillLevel(item.getSkillLevel());
                skill.setStatus(item.getStatus() != null ? item.getStatus() : CommonStatusEnum.ENABLE.getStatus());
                skills.add(skill);
            }
            deltaWorkerSkillService.validateSkillsNoDuplication(skills);
            deltaWorkerSkillService.replaceSkills(worker.getId(), skills);
        }

        // 5. 更新申请状态
        application.setApplicationStatus(WorkerAuditStatusEnum.APPROVED.getStatus());
        application.setReviewerId(reviewerId);
        application.setReviewedAt(LocalDateTime.now());
        deltaWorkerApplicationMapper.updateById(application);
        log.info("[approveApplication] 审核通过 application({}) -> worker({})", application.getId(), worker.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long applicationId, String rejectReason, Long reviewerId) {
        // 校验申请存在且为审核中
        DeltaWorkerApplicationDO application = validateApplicationPending(applicationId);
        // 更新为驳回
        application.setApplicationStatus(WorkerAuditStatusEnum.REJECTED.getStatus());
        application.setRejectReason(rejectReason);
        application.setReviewerId(reviewerId);
        application.setReviewedAt(LocalDateTime.now());
        deltaWorkerApplicationMapper.updateById(application);
        log.info("[rejectApplication] 审核驳回 application({}), reviewer({})", applicationId, reviewerId);
    }

    // ========== 内部校验 ==========

    private DeltaWorkerApplicationDO validateApplicationPending(Long applicationId) {
        DeltaWorkerApplicationDO application = deltaWorkerApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw exception(WORKER_APPLICATION_NOT_EXISTS);
        }
        if (!WorkerAuditStatusEnum.isPending(application.getApplicationStatus())) {
            throw exception(WORKER_APPLICATION_STATUS_ERROR);
        }
        return application;
    }

}
