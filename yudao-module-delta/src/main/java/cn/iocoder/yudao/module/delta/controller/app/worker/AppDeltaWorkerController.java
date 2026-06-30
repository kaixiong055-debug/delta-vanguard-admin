package cn.iocoder.yudao.module.delta.controller.app.worker;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.delta.controller.app.worker.vo.*;
import cn.iocoder.yudao.module.delta.convert.worker.DeltaWorkerApplicationConvert;
import cn.iocoder.yudao.module.delta.convert.worker.DeltaWorkerConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerApplicationService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 打手
 *
 * @author Delta-Vanguard
 */
@Tag(name = "用户 APP - 打手")
@RestController
@RequestMapping("/delta/worker")
@Validated
@Slf4j
public class AppDeltaWorkerController {

    @Resource
    private DeltaWorkerService deltaWorkerService;

    @Resource
    private DeltaWorkerApplicationService deltaWorkerApplicationService;

    @Resource
    private DeltaWorkerSkillService deltaWorkerSkillService;

    // ===================== 身份查询 =====================

    @GetMapping("/identity")
    @Operation(summary = "查询打手身份")
    public CommonResult<AppDeltaWorkerIdentityRespVO> getIdentity() {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = deltaWorkerService.getWorkerIdentity(userId);
        DeltaWorkerApplicationDO application = deltaWorkerApplicationService.getLatestApplicationByUserId(userId);

        AppDeltaWorkerIdentityRespVO respVO = new AppDeltaWorkerIdentityRespVO();
        if (worker != null) {
            respVO.setIsWorker(WorkerAuditStatusEnum.isApproved(worker.getAuditStatus()));
            respVO.setAuditStatus(worker.getAuditStatus());
            respVO.setWorkStatus(worker.getWorkStatus());
            respVO.setEnabledStatus(worker.getStatus());
            respVO.setWorkerId(worker.getId());
            respVO.setWorkerNo(worker.getWorkerNo());
            respVO.setDisplayName(worker.getDisplayName());
        } else {
            respVO.setIsWorker(false);
        }

        if (application != null) {
            respVO.setApplicationStatus(application.getApplicationStatus());
            respVO.setRejectReason(application.getRejectReason());
        } else {
            respVO.setApplicationStatus(WorkerAuditStatusEnum.NOT_APPLIED.getStatus());
        }

        return success(respVO);
    }

    // ===================== 申请管理 =====================

    @PostMapping("/apply")
    @Operation(summary = "提交打手申请")
    public CommonResult<Long> applyWorker(@RequestBody @Valid AppDeltaWorkerApplyReqVO reqVO) {
        Long applicationId = deltaWorkerApplicationService.applyWorker(getLoginUserId(), reqVO);
        return success(applicationId);
    }

    @GetMapping("/application")
    @Operation(summary = "查询当前申请")
    public CommonResult<AppDeltaWorkerApplicationRespVO> getMyApplication() {
        Long userId = getLoginUserId();
        DeltaWorkerApplicationDO application = deltaWorkerApplicationService.getLatestApplicationByUserId(userId);
        if (application == null) {
            AppDeltaWorkerApplicationRespVO empty = new AppDeltaWorkerApplicationRespVO();
            empty.setApplicationStatus(WorkerAuditStatusEnum.NOT_APPLIED.getStatus());
            return success(empty);
        }
        return success(DeltaWorkerApplicationConvert.INSTANCE.convert(application));
    }

    // ===================== 资料管理 =====================

    @GetMapping("/profile")
    @Operation(summary = "查询打手资料")
    public CommonResult<AppDeltaWorkerProfileRespVO> getMyProfile() {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(userId);
        if (worker == null) {
            AppDeltaWorkerProfileRespVO empty = new AppDeltaWorkerProfileRespVO();
            empty.setAuditStatus(WorkerAuditStatusEnum.NOT_APPLIED.getStatus());
            return success(empty);
        }
        AppDeltaWorkerProfileRespVO respVO = DeltaWorkerConvert.INSTANCE.convert(worker);
        List<DeltaWorkerSkillDO> skills = deltaWorkerSkillService.getSkillListByWorkerId(worker.getId());
        respVO.setSkills(DeltaWorkerConvert.INSTANCE.convertSkillList(skills));
        return success(respVO);
    }

    @PutMapping("/profile")
    @Operation(summary = "修改本人打手资料")
    public CommonResult<Boolean> updateMyProfile(@RequestBody @Valid AppDeltaWorkerProfileUpdateReqVO reqVO) {
        deltaWorkerService.updateMyProfile(getLoginUserId(), reqVO);
        return success(true);
    }

    // ===================== 工作状态 =====================

    @PutMapping("/work-status")
    @Operation(summary = "修改工作状态")
    public CommonResult<Boolean> updateWorkStatus(@RequestBody @Valid AppDeltaWorkerWorkStatusUpdateReqVO reqVO) {
        deltaWorkerService.updateMyWorkStatus(getLoginUserId(), reqVO.getWorkStatus());
        return success(true);
    }

}
