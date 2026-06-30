package cn.iocoder.yudao.module.delta.controller.admin.order;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryLogDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryMapper;
import cn.iocoder.yudao.module.delta.service.order.DeltaServiceOrderPhase9CoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Admin - 追回任务 Controller
 */
@Tag(name = "管理后台 - 人工追回任务")
@RestController
@RequestMapping("/delta/fund-recovery")
@Validated
public class DeltaFundRecoveryController {

    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;
    @Resource
    private DeltaFundRecoveryLogMapper deltaFundRecoveryLogMapper;
    @Resource
    private DeltaServiceOrderPhase9CoreService deltaServiceOrderPhase9CoreService;

    @GetMapping("/page")
    @Operation(summary = "分页查询追回任务")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:query')")
    public CommonResult<PageResult<DeltaFundRecoveryDO>> getFundRecoveryPage(
            @Valid DeltaFundRecoveryPageReqVO pageReqVO) {
        PageResult<DeltaFundRecoveryDO> pageResult = deltaFundRecoveryMapper.selectPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "查询追回任务详情")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:query')")
    public CommonResult<DeltaFundRecoveryDO> getFundRecoveryDetail(@RequestParam("id") Long id) {
        DeltaFundRecoveryDO recovery = deltaFundRecoveryMapper.selectById(id);
        return success(recovery);
    }

    @GetMapping("/logs")
    @Operation(summary = "查询追回操作日志")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:query')")
    public CommonResult<List<DeltaFundRecoveryLogDO>> getFundRecoveryLogs(@RequestParam("id") Long id) {
        List<DeltaFundRecoveryLogDO> logs = deltaFundRecoveryLogMapper.selectListByRecoveryId(id);
        return success(logs);
    }

    @PostMapping("/generate")
    @Operation(summary = "生成追回任务")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:generate')")
    public CommonResult<DeltaFundRecoveryDO> generateRecovery(
            @RequestBody @Valid DeltaFundRecoveryGenerateReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        DeltaFundRecoveryDO recovery = deltaServiceOrderPhase9CoreService
                .createRecoveryTask(adminUserId, reqVO.getAfterSaleId());
        return success(recovery);
    }

    @PostMapping("/start")
    @Operation(summary = "开始追回")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:process')")
    public CommonResult<Boolean> startRecovery(@RequestBody @Valid DeltaFundRecoveryStartReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.startRecovery(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/record")
    @Operation(summary = "记录追回结果")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:process')")
    public CommonResult<Boolean> recordRecovery(@RequestBody @Valid DeltaFundRecoveryRecordReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        String proofUrlsStr = reqVO.getProofUrls() != null ? StrUtil.join(",", reqVO.getProofUrls()) : null;
        deltaServiceOrderPhase9CoreService.recordRecovery(adminUserId, reqVO.getId(),
                reqVO.getRecoveredAmount(), reqVO.getRecoveryMethod(),
                reqVO.getExternalReference(), proofUrlsStr, reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/fail")
    @Operation(summary = "标记追回失败")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:process')")
    public CommonResult<Boolean> failRecovery(@RequestBody @Valid DeltaFundRecoveryFailReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.failRecovery(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

    @PostMapping("/retry")
    @Operation(summary = "重试追回")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:process')")
    public CommonResult<Boolean> retryRecovery(@RequestBody @Valid DeltaFundRecoveryRetryReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.retryRecovery(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消追回任务")
    @PreAuthorize("@ss.hasPermission('delta:fund-recovery:cancel')")
    public CommonResult<Boolean> cancelRecovery(@RequestBody @Valid DeltaFundRecoveryCancelReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.cancelRecovery(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

}
