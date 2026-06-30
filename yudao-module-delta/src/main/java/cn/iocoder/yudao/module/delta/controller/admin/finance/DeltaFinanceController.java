package cn.iocoder.yudao.module.delta.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.delta.service.finance.DeltaFinanceService;
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
 * 管理后台 - 财务汇总 Controller
 */
@Tag(name = "管理后台 - 财务汇总")
@RestController
@RequestMapping("/delta/finance")
@Validated
public class DeltaFinanceController {

    @Resource
    private DeltaFinanceService deltaFinanceService;

    @GetMapping("/summary")
    @Operation(summary = "财务总览")
    @PreAuthorize("@ss.hasPermission('delta:finance:query')")
    public CommonResult<DeltaFinanceSummaryRespVO> summary(
            @Valid DeltaFinanceSummaryReqVO reqVO) {
        return success(deltaFinanceService.getSummary(reqVO));
    }

    @GetMapping("/trend")
    @Operation(summary = "财务趋势")
    @PreAuthorize("@ss.hasPermission('delta:finance:query')")
    public CommonResult<List<DeltaFinanceTrendItemRespVO>> trend(
            @Valid DeltaFinanceTrendReqVO reqVO) {
        return success(deltaFinanceService.getTrend(reqVO));
    }

    @GetMapping("/settlement-summary")
    @Operation(summary = "结算汇总")
    @PreAuthorize("@ss.hasPermission('delta:finance:query')")
    public CommonResult<DeltaFinanceSettlementSummaryRespVO> settlementSummary(
            @Valid DeltaFinanceSummaryReqVO reqVO) {
        return success(deltaFinanceService.getSettlementSummary(reqVO));
    }

    @GetMapping("/refund-summary")
    @Operation(summary = "退款汇总")
    @PreAuthorize("@ss.hasPermission('delta:finance:query')")
    public CommonResult<DeltaFinanceRefundSummaryRespVO> refundSummary(
            @Valid DeltaFinanceSummaryReqVO reqVO) {
        return success(deltaFinanceService.getRefundSummary(reqVO));
    }

    @GetMapping("/recovery-summary")
    @Operation(summary = "追回汇总")
    @PreAuthorize("@ss.hasPermission('delta:finance:query')")
    public CommonResult<DeltaFinanceRecoverySummaryRespVO> recoverySummary(
            @Valid DeltaFinanceSummaryReqVO reqVO) {
        return success(deltaFinanceService.getRecoverySummary(reqVO));
    }

}
