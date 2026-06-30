package cn.iocoder.yudao.module.delta.controller.app.clubapplication;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo.*;
import cn.iocoder.yudao.module.delta.convert.club.DeltaClubApplicationConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 APP - 俱乐部入驻申请
 *
 * @author Delta-Vanguard
 */
@Tag(name = "用户 APP - 俱乐部入驻申请")
@RestController
@RequestMapping("/delta/club-application")
@Validated
@Slf4j
public class AppDeltaClubApplicationController {

    @Resource
    private DeltaClubApplicationService deltaClubApplicationService;

    // ===================== 提交申请 =====================

    @PostMapping("/submit")
    @Operation(summary = "提交俱乐部入驻申请")
    public CommonResult<Long> submitApplication(@RequestBody @Valid AppDeltaClubApplicationSubmitReqVO reqVO) {
        Long applicationId = deltaClubApplicationService.submitApplication(getLoginUserId(), reqVO);
        return success(applicationId);
    }

    // ===================== 我的申请 =====================

    @GetMapping("/get")
    @Operation(summary = "查询我的最新入驻申请")
    public CommonResult<AppDeltaClubApplicationRespVO> getMyApplication() {
        Long memberId = getLoginUserId();
        DeltaClubApplicationDO application = deltaClubApplicationService.getMyLatestApplication(memberId);
        if (application == null) {
            AppDeltaClubApplicationRespVO empty = new AppDeltaClubApplicationRespVO();
            empty.setApplicationStatus(0);
            empty.setApplicationStatusName("未申请");
            return success(empty);
        }
        return success(DeltaClubApplicationConvert.INSTANCE.convertApp(application));
    }

    // ===================== 撤销申请 =====================

    @PostMapping("/cancel")
    @Operation(summary = "撤销入驻申请")
    public CommonResult<Boolean> cancelApplication() {
        deltaClubApplicationService.cancelApplication(getLoginUserId());
        return success(true);
    }

}
