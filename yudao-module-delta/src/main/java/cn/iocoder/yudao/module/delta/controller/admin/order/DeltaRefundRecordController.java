package cn.iocoder.yudao.module.delta.controller.admin.order;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundRecordMapper;
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
 * Admin - 退款记录 Controller
 */
@Tag(name = "管理后台 - 人工退款记录")
@RestController
@RequestMapping("/delta/refund-record")
@Validated
public class DeltaRefundRecordController {

    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaRefundLogMapper deltaRefundLogMapper;
    @Resource
    private DeltaServiceOrderPhase9CoreService deltaServiceOrderPhase9CoreService;

    @GetMapping("/page")
    @Operation(summary = "分页查询退款记录")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:query')")
    public CommonResult<PageResult<DeltaRefundRecordDO>> getRefundRecordPage(
            @Valid DeltaRefundRecordPageReqVO pageReqVO) {
        PageResult<DeltaRefundRecordDO> pageResult = deltaRefundRecordMapper.selectPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "查询退款记录详情")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:query')")
    public CommonResult<DeltaRefundRecordDO> getRefundRecordDetail(@RequestParam("id") Long id) {
        DeltaRefundRecordDO record = deltaRefundRecordMapper.selectById(id);
        return success(record);
    }

    @GetMapping("/logs")
    @Operation(summary = "查询退款操作日志")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:query')")
    public CommonResult<List<DeltaRefundLogDO>> getRefundRecordLogs(@RequestParam("id") Long id) {
        List<DeltaRefundLogDO> logs = deltaRefundLogMapper.selectListByRefundRecordId(id);
        return success(logs);
    }

    @PostMapping("/start")
    @Operation(summary = "开始处理退款")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:process')")
    public CommonResult<Boolean> startRefund(@RequestBody @Valid DeltaRefundRecordStartReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.startRefund(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/complete")
    @Operation(summary = "标记退款完成")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:complete')")
    public CommonResult<Boolean> completeRefund(@RequestBody @Valid DeltaRefundRecordCompleteReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        String proofUrlsStr = reqVO.getProofUrls() != null ? StrUtil.join(",", reqVO.getProofUrls()) : null;
        deltaServiceOrderPhase9CoreService.completeRefund(adminUserId, reqVO.getId(),
                reqVO.getRefundMethod(), reqVO.getExternalReference(), proofUrlsStr, reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/fail")
    @Operation(summary = "标记退款失败")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:process')")
    public CommonResult<Boolean> failRefund(@RequestBody @Valid DeltaRefundRecordFailReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.failRefund(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

    @PostMapping("/retry")
    @Operation(summary = "重新处理退款")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:process')")
    public CommonResult<Boolean> retryRefund(@RequestBody @Valid DeltaRefundRecordRetryReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.retryRefund(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤销退款记录")
    @PreAuthorize("@ss.hasPermission('delta:refund-record:cancel')")
    public CommonResult<Boolean> cancelRefund(@RequestBody @Valid DeltaRefundRecordCancelReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderPhase9CoreService.cancelRefund(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

}
