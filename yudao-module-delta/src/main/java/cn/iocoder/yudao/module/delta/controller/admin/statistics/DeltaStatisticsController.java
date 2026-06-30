package cn.iocoder.yudao.module.delta.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.delta.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.delta.service.statistics.DeltaStatisticsService;
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
 * 管理后台 - 运营统计 Controller
 */
@Tag(name = "管理后台 - 运营统计")
@RestController
@RequestMapping("/delta/statistics")
@Validated
public class DeltaStatisticsController {

    @Resource
    private DeltaStatisticsService deltaStatisticsService;

    @GetMapping("/overview")
    @Operation(summary = "运营总览")
    @PreAuthorize("@ss.hasPermission('delta:statistics:query')")
    public CommonResult<DeltaStatisticsOverviewRespVO> overview(
            @Valid DeltaStatisticsDateReqVO reqVO) {
        return success(deltaStatisticsService.getOverview(reqVO));
    }

    @GetMapping("/order-trend")
    @Operation(summary = "订单趋势")
    @PreAuthorize("@ss.hasPermission('delta:statistics:query')")
    public CommonResult<List<DeltaStatisticsTrendItemRespVO>> orderTrend(
            @Valid DeltaStatisticsTrendReqVO reqVO) {
        return success(deltaStatisticsService.getOrderTrend(reqVO));
    }

    @GetMapping("/order-status-distribution")
    @Operation(summary = "订单状态分布")
    @PreAuthorize("@ss.hasPermission('delta:statistics:query')")
    public CommonResult<List<DeltaStatisticsOrderStatusDistributionRespVO>> orderStatusDistribution(
            @Valid DeltaStatisticsDateReqVO reqVO) {
        return success(deltaStatisticsService.getOrderStatusDistribution(reqVO));
    }

    @GetMapping("/after-sale-summary")
    @Operation(summary = "售后与资金统计")
    @PreAuthorize("@ss.hasPermission('delta:statistics:query')")
    public CommonResult<DeltaStatisticsAfterSaleSummaryRespVO> afterSaleSummary(
            @Valid DeltaStatisticsDateReqVO reqVO) {
        return success(deltaStatisticsService.getAfterSaleSummary(reqVO));
    }

    @GetMapping("/worker-ranking")
    @Operation(summary = "打手排行")
    @PreAuthorize("@ss.hasPermission('delta:statistics:query')")
    public CommonResult<List<DeltaStatisticsWorkerRankingItemRespVO>> workerRanking(
            @Valid DeltaStatisticsWorkerRankingReqVO reqVO) {
        return success(deltaStatisticsService.getWorkerRanking(reqVO));
    }

}
