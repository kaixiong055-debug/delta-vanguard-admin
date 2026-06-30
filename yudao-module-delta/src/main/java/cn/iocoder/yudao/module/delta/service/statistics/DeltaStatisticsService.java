package cn.iocoder.yudao.module.delta.service.statistics;

import cn.iocoder.yudao.module.delta.controller.admin.statistics.vo.*;

import java.util.List;

/**
 * 运营统计 Service
 */
public interface DeltaStatisticsService {

    /** 运营总览 */
    DeltaStatisticsOverviewRespVO getOverview(DeltaStatisticsDateReqVO reqVO);

    /** 订单趋势 */
    List<DeltaStatisticsTrendItemRespVO> getOrderTrend(DeltaStatisticsTrendReqVO reqVO);

    /** 订单状态分布 */
    List<DeltaStatisticsOrderStatusDistributionRespVO> getOrderStatusDistribution(DeltaStatisticsDateReqVO reqVO);

    /** 售后与资金统计 */
    DeltaStatisticsAfterSaleSummaryRespVO getAfterSaleSummary(DeltaStatisticsDateReqVO reqVO);

    /** 打手排行 */
    List<DeltaStatisticsWorkerRankingItemRespVO> getWorkerRanking(DeltaStatisticsWorkerRankingReqVO reqVO);

}
