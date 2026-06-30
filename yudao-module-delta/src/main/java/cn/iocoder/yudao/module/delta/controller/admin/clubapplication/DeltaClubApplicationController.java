package cn.iocoder.yudao.module.delta.controller.admin.clubapplication;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.*;
import cn.iocoder.yudao.module.delta.convert.club.DeltaClubApplicationConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubApplicationService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - 俱乐部入驻申请管理
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 俱乐部入驻申请管理")
@RestController
@RequestMapping("/delta/club-application")
@Validated
@Slf4j
public class DeltaClubApplicationController {

    @Resource
    private DeltaClubApplicationService deltaClubApplicationService;

    @Resource
    private MemberUserApi memberUserApi;

    // ===================== 分页查询 =====================

    @GetMapping("/page")
    @Operation(summary = "获得俱乐部入驻申请分页列表")
    @PreAuthorize("@ss.hasPermission('delta:club-application:query')")
    public CommonResult<PageResult<DeltaClubApplicationRespVO>> getApplicationPage(
            @Valid DeltaClubApplicationPageReqVO pageReqVO) {
        PageResult<DeltaClubApplicationDO> pageResult = deltaClubApplicationService.getApplicationPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 批量查询会员信息（反 N+1）
        Set<Long> memberIds = pageResult.getList().stream()
                .map(DeltaClubApplicationDO::getApplicantMemberId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberUserMap = memberUserApi.getUserMap(memberIds);
        return success(DeltaClubApplicationConvert.INSTANCE.convertPage(pageResult, memberUserMap));
    }

    // ===================== 详情查询 =====================

    @GetMapping("/get")
    @Operation(summary = "获得俱乐部入驻申请详情")
    @Parameter(name = "id", description = "申请ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('delta:club-application:query')")
    public CommonResult<DeltaClubApplicationRespVO> getApplication(@RequestParam("id") Long id) {
        DeltaClubApplicationDO application = deltaClubApplicationService.getApplication(id);
        if (application == null) {
            return success(null);
        }
        MemberUserRespDTO memberUser = memberUserApi.getUser(application.getApplicantMemberId());
        return success(DeltaClubApplicationConvert.INSTANCE.convert(application, memberUser));
    }

    // ===================== 审核操作 =====================

    @PostMapping("/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("@ss.hasPermission('delta:club-application:audit')")
    public CommonResult<Boolean> approveApplication(@RequestBody @Valid DeltaClubApplicationApproveReqVO reqVO) {
        deltaClubApplicationService.approveApplication(reqVO, getLoginUserId());
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "审核拒绝")
    @PreAuthorize("@ss.hasPermission('delta:club-application:audit')")
    public CommonResult<Boolean> rejectApplication(@RequestBody @Valid DeltaClubApplicationRejectReqVO reqVO) {
        deltaClubApplicationService.rejectApplication(reqVO.getId(), reqVO.getReason(), getLoginUserId());
        return success(true);
    }

}
