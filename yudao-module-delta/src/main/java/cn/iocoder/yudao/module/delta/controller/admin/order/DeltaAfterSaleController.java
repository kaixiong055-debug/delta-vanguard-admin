package cn.iocoder.yudao.module.delta.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleDO;
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
 * Admin - 售后案件 Controller
 */
@Tag(name = "管理后台 - 售后案件")
@RestController
@RequestMapping("/delta/after-sale")
@Validated
public class DeltaAfterSaleController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;

    @GetMapping("/page")
    @Operation(summary = "分页查询售后案件")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:query')")
    public CommonResult<PageResult<DeltaAfterSaleDO>> getAfterSalePage(
            @Valid DeltaAfterSalePageReqVO pageReqVO) {
        PageResult<DeltaAfterSaleDO> pageResult = deltaServiceOrderService.getAfterSalePage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "查询售后案件详情")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:query')")
    public CommonResult<DeltaAfterSaleDO> getAfterSaleDetail(@RequestParam("id") Long id) {
        DeltaAfterSaleDO afterSale = deltaServiceOrderService.getAfterSaleDetail(id);
        return success(afterSale);
    }

    @PostMapping("/accept")
    @Operation(summary = "受理售后")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:accept')")
    public CommonResult<Boolean> acceptAfterSale(
            @RequestBody @Valid DeltaAfterSaleAcceptReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.acceptAfterSaleByAdmin(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回售后")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:reject')")
    public CommonResult<Boolean> rejectAfterSale(
            @RequestBody @Valid DeltaAfterSaleRejectReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.rejectAfterSaleByAdmin(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

    @PostMapping("/arbitrate")
    @Operation(summary = "仲裁售后")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:arbitrate')")
    public CommonResult<Boolean> arbitrateAfterSale(
            @RequestBody @Valid DeltaAfterSaleArbitrateReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.arbitrateAfterSaleByAdmin(adminUserId, reqVO.getId(),
                reqVO.getDecisionType(), reqVO.getRefundAmount(),
                reqVO.getResponsibilityType(), reqVO.getWorkerDeductionAmount(),
                reqVO.getPlatformBearAmount(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭售后案件")
    @PreAuthorize("@ss.hasPermission('delta:after-sale:close')")
    public CommonResult<Boolean> closeAfterSale(
            @RequestBody @Valid DeltaAfterSaleCloseReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.closeAfterSaleByAdmin(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

}
