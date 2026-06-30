package cn.iocoder.yudao.module.delta.service.finance;

import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;

import java.util.List;

/**
 * 财务汇总 Service
 */
public interface DeltaFinanceService {

    /** 财务总览 */
    DeltaFinanceSummaryRespVO getSummary(DeltaFinanceSummaryReqVO reqVO);

    /** 财务趋势 */
    List<DeltaFinanceTrendItemRespVO> getTrend(DeltaFinanceTrendReqVO reqVO);

    /** 结算汇总 */
    DeltaFinanceSettlementSummaryRespVO getSettlementSummary(DeltaFinanceSummaryReqVO reqVO);

    /** 退款汇总 */
    DeltaFinanceRefundSummaryRespVO getRefundSummary(DeltaFinanceSummaryReqVO reqVO);

    /** 追回汇总 */
    DeltaFinanceRecoverySummaryRespVO getRecoverySummary(DeltaFinanceSummaryReqVO reqVO);

}
