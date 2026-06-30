package cn.iocoder.yudao.module.delta.service.finance;

import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.delta.dal.mysql.statistics.DeltaStatisticsQueryMapper;
import cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.delta.enums.settlement.SettlementStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.RefundStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.RecoveryStatusEnum;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundRecordMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 财务汇总 Service 实现
 */
@Service
@Validated
@Slf4j
public class DeltaFinanceServiceImpl implements DeltaFinanceService {

    @Resource
    private DeltaStatisticsQueryMapper deltaStatisticsQueryMapper;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;
    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;

    private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return;
        if (startTime.isAfter(endTime)) {
            throw exception(ErrorCodeConstants.STATISTICS_DATE_RANGE_INVALID);
        }
        long days = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate());
        if (days > 366) {
            throw exception(ErrorCodeConstants.STATISTICS_RANGE_TOO_LARGE);
        }
    }

    // ====== 财务总览 ======

    @Override
    public DeltaFinanceSummaryRespVO getSummary(DeltaFinanceSummaryReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaFinanceSummaryRespVO vo = new DeltaFinanceSummaryRespVO();

        // 服务订单
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .between(DeltaServiceOrderDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setServiceOrderAmount(orders.stream().filter(o -> o.getServiceAmount() != null)
                .mapToLong(o -> o.getServiceAmount()).sum());
        vo.setPlatformFeeAmount(orders.stream().filter(o -> o.getPlatformFee() != null)
                .mapToLong(o -> o.getPlatformFee()).sum());
        vo.setWorkerIncomeAmount(orders.stream().filter(o -> o.getWorkerAmount() != null)
                .mapToLong(o -> o.getWorkerAmount()).sum());

        // 结算
        List<DeltaWorkerSettlementDO> settlements = deltaWorkerSettlementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaWorkerSettlementDO>()
                        .between(DeltaWorkerSettlementDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setSettlementAmount(settlements.stream().filter(s -> s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setApprovedSettlementAmount(settlements.stream()
                .filter(s -> SettlementStatusEnum.APPROVED.getStatus().equals(s.getSettlementStatus())
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setPaidSettlementAmount(settlements.stream()
                .filter(s -> SettlementStatusEnum.PAID.getStatus().equals(s.getSettlementStatus())
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setPendingSettlementAmount(settlements.stream()
                .filter(s -> SettlementStatusEnum.PENDING_REVIEW.getStatus().equals(s.getSettlementStatus())
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());

        // 退款
        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .between(DeltaRefundRecordDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setRefundAmount(refunds.stream().filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());
        vo.setCompletedRefundAmount(refunds.stream()
                .filter(r -> RefundStatusEnum.MANUAL_COMPLETED.getStatus().equals(r.getRefundStatus())
                        && r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());
        vo.setPendingRefundAmount(refunds.stream()
                .filter(r -> RefundStatusEnum.PENDING_MANUAL.getStatus().equals(r.getRefundStatus())
                        || RefundStatusEnum.MANUAL_PROCESSING.getStatus().equals(r.getRefundStatus()))
                .filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());

        // 追回
        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .between(DeltaFundRecoveryDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setShouldRecoverAmount(recoveries.stream().filter(r -> r.getShouldRecoverAmount() != null)
                .mapToLong(r -> r.getShouldRecoverAmount()).sum());
        vo.setRecoveredAmount(recoveries.stream().filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount()).sum());
        vo.setRemainingRecoveryAmount(recoveries.stream().filter(r -> r.getRemainingAmount() != null)
                .mapToLong(r -> r.getRemainingAmount()).sum());

        return vo;
    }

    // ====== 财务趋势 ======

    @Override
    public List<DeltaFinanceTrendItemRespVO> getTrend(DeltaFinanceTrendReqVO reqVO) {
        String granularity = reqVO.getGranularity() != null ? reqVO.getGranularity().toUpperCase() : "DAY";
        if (!Arrays.asList("DAY", "WEEK", "MONTH").contains(granularity)) {
            throw exception(ErrorCodeConstants.STATISTICS_GRANULARITY_INVALID);
        }
        LocalDateTime startTime = reqVO.getStartDate().atStartOfDay();
        LocalDateTime endTime = reqVO.getEndDate().plusDays(1).atStartOfDay();
        validateDateRange(startTime, endTime);

        // 目前仅支持按天（财务趋势按天最常用，按周/月可后续扩展）
        List<Map<String, Object>> serviceRows = deltaStatisticsQueryMapper
                .selectFinanceTrendServiceByDay(startTime, endTime);
        List<Map<String, Object>> settlementRows = deltaStatisticsQueryMapper
                .selectFinanceTrendSettlementByDay(startTime, endTime);
        List<Map<String, Object>> refundRows = deltaStatisticsQueryMapper
                .selectFinanceTrendRefundByDay(startTime, endTime);
        List<Map<String, Object>> recoveryRows = deltaStatisticsQueryMapper
                .selectFinanceTrendRecoveryByDay(startTime, endTime);

        Map<String, Map<String, Object>> settlementMap = settlementRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"), r -> r, (a, b) -> a, LinkedHashMap::new));
        Map<String, Long> refundMap = refundRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"),
                        r -> toLong(r.get("refund_amount")), (a, b) -> a, LinkedHashMap::new));
        Map<String, Long> recoveryMap = recoveryRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"),
                        r -> toLong(r.get("recovered_amount")), (a, b) -> a, LinkedHashMap::new));

        List<DeltaFinanceTrendItemRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : serviceRows) {
            String date = (String) row.get("date_val");
            Map<String, Object> settlement = settlementMap.get(date);
            DeltaFinanceTrendItemRespVO item = new DeltaFinanceTrendItemRespVO();
            item.setDate(date);
            item.setServiceAmount(toLong(row.get("service_amount")));
            item.setPlatformFeeAmount(toLong(row.get("platform_fee")));
            item.setWorkerIncomeAmount(toLong(row.get("worker_income")));
            item.setSettlementAmount(settlement != null ? toLong(settlement.get("settlement_amount")) : 0L);
            item.setPaidSettlementAmount(settlement != null ? toLong(settlement.get("paid_amount")) : 0L);
            item.setRefundAmount(refundMap.getOrDefault(date, 0L));
            item.setRecoveredAmount(recoveryMap.getOrDefault(date, 0L));
            result.add(item);
        }
        return result;
    }

    // ====== 结算汇总 ======

    @Override
    public DeltaFinanceSettlementSummaryRespVO getSettlementSummary(DeltaFinanceSummaryReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaFinanceSettlementSummaryRespVO vo = new DeltaFinanceSettlementSummaryRespVO();

        List<DeltaWorkerSettlementDO> settlements = deltaWorkerSettlementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaWorkerSettlementDO>()
                        .between(DeltaWorkerSettlementDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));

        vo.setTotalCount((long) settlements.size());
        vo.setPendingCount(countBySettlementStatus(settlements, 0));
        vo.setApprovedCount(countBySettlementStatus(settlements, 1));
        vo.setRejectedCount(countBySettlementStatus(settlements, 2));
        vo.setPaidCount(countBySettlementStatus(settlements, 3));
        vo.setCanceledCount(countBySettlementStatus(settlements, 4));

        vo.setTotalAmount(settlements.stream().filter(s -> s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setApprovedAmount(settlements.stream()
                .filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == 1
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setPaidAmount(settlements.stream()
                .filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == 3
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setPendingAmount(settlements.stream()
                .filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == 0
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());

        return vo;
    }

    // ====== 退款汇总 ======

    @Override
    public DeltaFinanceRefundSummaryRespVO getRefundSummary(DeltaFinanceSummaryReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaFinanceRefundSummaryRespVO vo = new DeltaFinanceRefundSummaryRespVO();

        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .between(DeltaRefundRecordDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));

        vo.setTotalCount((long) refunds.size());
        vo.setPendingManualCount(countByRefundStatus(refunds, 0));
        vo.setProcessingCount(countByRefundStatus(refunds, 1));
        vo.setCompletedCount(countByRefundStatus(refunds, 2));
        vo.setCanceledCount(countByRefundStatus(refunds, 3));
        vo.setFailedCount(countByRefundStatus(refunds, 4));

        vo.setTotalRefundAmount(refunds.stream().filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());
        vo.setCompletedRefundAmount(refunds.stream()
                .filter(r -> r.getRefundStatus() != null && r.getRefundStatus() == 2
                        && r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());
        vo.setPendingRefundAmount(refunds.stream()
                .filter(r -> r.getRefundStatus() != null && (r.getRefundStatus() == 0 || r.getRefundStatus() == 1)
                        && r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());

        return vo;
    }

    // ====== 追回汇总 ======

    @Override
    public DeltaFinanceRecoverySummaryRespVO getRecoverySummary(DeltaFinanceSummaryReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaFinanceRecoverySummaryRespVO vo = new DeltaFinanceRecoverySummaryRespVO();

        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .between(DeltaFundRecoveryDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));

        vo.setTotalCount((long) recoveries.size());
        vo.setPendingCount(countByRecoveryStatus(recoveries, 0));
        vo.setProcessingCount(countByRecoveryStatus(recoveries, 1));
        vo.setPartiallyRecoveredCount(countByRecoveryStatus(recoveries, 2));
        vo.setRecoveredCount(countByRecoveryStatus(recoveries, 3));
        vo.setFailedCount(countByRecoveryStatus(recoveries, 4));
        vo.setCanceledCount(countByRecoveryStatus(recoveries, 5));

        vo.setShouldRecoverAmount(recoveries.stream().filter(r -> r.getShouldRecoverAmount() != null)
                .mapToLong(r -> r.getShouldRecoverAmount()).sum());
        vo.setRecoveredAmount(recoveries.stream().filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount()).sum());
        vo.setRemainingAmount(recoveries.stream().filter(r -> r.getRemainingAmount() != null)
                .mapToLong(r -> r.getRemainingAmount()).sum());

        return vo;
    }

    private static long countBySettlementStatus(List<DeltaWorkerSettlementDO> list, int status) {
        return list.stream().filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == status).count();
    }

    private static long countByRefundStatus(List<DeltaRefundRecordDO> list, int status) {
        return list.stream().filter(r -> r.getRefundStatus() != null && r.getRefundStatus() == status).count();
    }

    private static long countByRecoveryStatus(List<DeltaFundRecoveryDO> list, int status) {
        return list.stream().filter(r -> r.getRecoveryStatus() != null && r.getRecoveryStatus() == status).count();
    }

    private static Long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

}
