package cn.iocoder.yudao.module.delta.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderCancelDO;
import cn.iocoder.yudao.module.delta.service.order.DeltaServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Admin - 取消申请 Controller
 */
@Tag(name = "管理后台 - 取消申请")
@RestController
@RequestMapping("/delta/order-cancel")
@Validated
public class DeltaOrderCancelController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;

    @GetMapping("/page")
    @Operation(summary = "分页查询取消申请")
    @PreAuthorize("@ss.hasPermission('delta:order-cancel:query')")
    public CommonResult<PageResult<DeltaOrderCancelDO>> getCancelPage(
            @Valid DeltaOrderCancelPageReqVO pageReqVO) {
        PageResult<DeltaOrderCancelDO> pageResult = deltaServiceOrderService.getCancelPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "查询取消申请详情")
    @PreAuthorize("@ss.hasPermission('delta:order-cancel:query')")
    public CommonResult<DeltaOrderCancelDO> getCancelDetail(@RequestParam("id") Long id) {
        DeltaOrderCancelDO cancel = deltaServiceOrderService.getCancelDetail(id);
        return success(cancel);
    }

    @PostMapping("/approve")
    @Operation(summary = "批准取消")
    @PreAuthorize("@ss.hasPermission('delta:order-cancel:approve')")
    public CommonResult<Boolean> approveCancel(
            @RequestBody @Valid DeltaOrderCancelApproveReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.approveCancelByAdmin(adminUserId, reqVO.getId(),
                reqVO.getRefundAmount(), reqVO.getResponsibilityType(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回取消")
    @PreAuthorize("@ss.hasPermission('delta:order-cancel:reject')")
    public CommonResult<Boolean> rejectCancel(
            @RequestBody @Valid DeltaOrderCancelRejectReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.rejectCancelByAdmin(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

}
