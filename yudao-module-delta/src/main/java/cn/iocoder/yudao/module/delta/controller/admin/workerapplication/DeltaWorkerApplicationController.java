package cn.iocoder.yudao.module.delta.controller.admin.workerapplication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.*;
import cn.iocoder.yudao.module.delta.convert.worker.DeltaWorkerApplicationConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerApplicationService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - 打手申请管理
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 打手申请管理")
@RestController
@RequestMapping("/delta/worker-application")
@Validated
@Slf4j
public class DeltaWorkerApplicationController {

    @Resource
    private DeltaWorkerApplicationService deltaWorkerApplicationService;

    @Resource
    private MemberUserApi memberUserApi;

    // ===================== 分页查询 =====================

    @GetMapping("/page")
    @Operation(summary = "获得打手申请分页列表")
    @PreAuthorize("@ss.hasPermission('delta:worker-application:query')")
    public CommonResult<PageResult<DeltaWorkerApplicationRespVO>> getApplicationPage(
            @Valid DeltaWorkerApplicationPageReqVO pageReqVO) {
        PageResult<DeltaWorkerApplicationDO> pageResult = deltaWorkerApplicationService.getApplicationPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 批量查询会员信息（反 N+1）
        Set<Long> userIds = pageResult.getList().stream()
                .map(DeltaWorkerApplicationDO::getUserId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberUserMap = memberUserApi.getUserMap(userIds);
        return success(DeltaWorkerApplicationConvert.INSTANCE.convertPage2(pageResult, memberUserMap));
    }

    // ===================== 详情查询 =====================

    @GetMapping("/get")
    @Operation(summary = "获得打手申请详情")
    @Parameter(name = "id", description = "申请ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('delta:worker-application:query')")
    public CommonResult<DeltaWorkerApplicationRespVO> getApplication(@RequestParam("id") Long id) {
        DeltaWorkerApplicationDO application = deltaWorkerApplicationService.getApplication(id);
        if (application == null) {
            return success(null);
        }
        // 查询会员信息
        MemberUserRespDTO memberUser = memberUserApi.getUser(application.getUserId());
        return success(DeltaWorkerApplicationConvert.INSTANCE.convert2(application, memberUser));
    }

    // ===================== 审核操作 =====================

    @PostMapping("/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("@ss.hasPermission('delta:worker-application:approve')")
    public CommonResult<Boolean> approveApplication(@RequestBody @Valid DeltaWorkerApplicationApproveReqVO reqVO) {
        deltaWorkerApplicationService.approveApplication(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "审核驳回")
    @PreAuthorize("@ss.hasPermission('delta:worker-application:reject')")
    public CommonResult<Boolean> rejectApplication(@RequestBody @Valid DeltaWorkerApplicationRejectReqVO reqVO) {
        deltaWorkerApplicationService.rejectApplication(reqVO.getApplicationId(), reqVO.getRejectReason(),
                getLoginUserId());
        return success(true);
    }

}
