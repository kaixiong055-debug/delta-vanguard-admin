package cn.iocoder.yudao.module.delta.service.statistics;

import cn.iocoder.yudao.module.delta.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.delta.dal.mysql.statistics.DeltaStatisticsQueryMapper;
import cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.delta.enums.order.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaAfterSaleMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundRecordMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 运营统计 Service 实现
 */
@Service
@Validated
@Slf4j
public class DeltaStatisticsServiceImpl implements DeltaStatisticsService {

    @Resource
    private DeltaStatisticsQueryMapper deltaStatisticsQueryMapper;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaAfterSaleMapper deltaAfterSaleMapper;
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;
    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;
    @Resource
    private DeltaWorkerService deltaWorkerService;

    // ====== 校验 ======

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

    // ====== 运营总览 ======

    @Override
    public DeltaStatisticsOverviewRespVO getOverview(DeltaStatisticsDateReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaStatisticsOverviewRespVO vo = new DeltaStatisticsOverviewRespVO();

        // 服务订单统计
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .between(DeltaServiceOrderDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));

        vo.setTotalOrderCount((long) orders.size());
        vo.setPendingClaimCount(countByStatus(orders, ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus(),
                ServiceOrderStatusEnum.POOL_PENDING.getStatus()));
        vo.setProcessingOrderCount(countByStatus(orders,
                ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                ServiceOrderStatusEnum.PENDING_VERIFICATION.getStatus()));
        vo.setPendingAcceptanceCount(countByStatus(orders, ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                ServiceOrderStatusEnum.PENDING_VERIFICATION.getStatus()));
        vo.setCompletedOrderCount(countByStatus(orders, ServiceOrderStatusEnum.COMPLETED.getStatus()));
        vo.setCanceledOrderCount(countByStatus(orders, ServiceOrderStatusEnum.CANCELED.getStatus()));
        vo.setAfterSaleOrderCount(countByStatus(orders, ServiceOrderStatusEnum.AFTER_SALE.getStatus(),
                ServiceOrderStatusEnum.DISPUTE.getStatus()));

        long totalAmount = orders.stream().filter(o -> o.getServiceAmount() != null)
                .mapToLong(DeltaServiceOrderDO::getServiceAmount).sum();
        long completedAmount = orders.stream()
                .filter(o -> ServiceOrderStatusEnum.COMPLETED.getStatus().equals(o.getStatus())
                        && o.getServiceAmount() != null)
                .mapToLong(DeltaServiceOrderDO::getServiceAmount).sum();
        vo.setTotalServiceAmount(totalAmount);
        vo.setCompletedServiceAmount(completedAmount);

        // 售后统计
        List<DeltaAfterSaleDO> afterSales = deltaAfterSaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaAfterSaleDO>()
                        .between(DeltaAfterSaleDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setAfterSaleCount((long) afterSales.size());

        // 退款统计
        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .between(DeltaRefundRecordDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setRefundCount((long) refunds.size());
        vo.setRefundAmount(refunds.stream().filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());

        // 追回统计
        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .between(DeltaFundRecoveryDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setRecoveryTaskCount((long) recoveries.size());
        vo.setRecoveredAmount(recoveries.stream().filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount()).sum());

        // 结算统计
        List<DeltaWorkerSettlementDO> settlements = deltaWorkerSettlementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaWorkerSettlementDO>()
                        .between(DeltaWorkerSettlementDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setWorkerSettlementCount((long) settlements.size());
        vo.setSettlementAmount(settlements.stream().filter(s -> s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());
        vo.setPaidSettlementAmount(settlements.stream()
                .filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == 3
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount()).sum());

        return vo;
    }

    @SafeVarargs
    private static long countByStatus(List<DeltaServiceOrderDO> orders, Integer... statuses) {
        Set<Integer> statusSet = new HashSet<>(Arrays.asList(statuses));
        return orders.stream().filter(o -> statusSet.contains(o.getStatus())).count();
    }

    // ====== 订单趋势 ======

    @Override
    public List<DeltaStatisticsTrendItemRespVO> getOrderTrend(DeltaStatisticsTrendReqVO reqVO) {
        String granularity = reqVO.getGranularity() != null ? reqVO.getGranularity().toUpperCase() : "DAY";
        LocalDateTime startTime = reqVO.getStartDate().atStartOfDay();
        LocalDateTime endTime = reqVO.getEndDate().plusDays(1).atStartOfDay();

        validateDateRange(startTime, endTime);

        // 订单趋势
        List<Map<String, Object>> orderRows;
        if ("WEEK".equals(granularity)) {
            orderRows = deltaStatisticsQueryMapper.selectOrderTrendByWeek(startTime, endTime);
        } else if ("MONTH".equals(granularity)) {
            orderRows = deltaStatisticsQueryMapper.selectOrderTrendByMonth(startTime, endTime);
        } else {
            orderRows = deltaStatisticsQueryMapper.selectOrderTrendByDay(startTime, endTime);
        }

        // 售后趋势
        List<Map<String, Object>> afterSaleRows;
        if ("WEEK".equals(granularity)) {
            afterSaleRows = deltaStatisticsQueryMapper.selectAfterSaleTrendByWeek(startTime, endTime);
        } else if ("MONTH".equals(granularity)) {
            afterSaleRows = deltaStatisticsQueryMapper.selectAfterSaleTrendByMonth(startTime, endTime);
        } else {
            afterSaleRows = deltaStatisticsQueryMapper.selectAfterSaleTrendByDay(startTime, endTime);
        }
        Map<String, Long> afterSaleMap = afterSaleRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"),
                        r -> toLong(r.get("after_sale_count")), (a, b) -> a, LinkedHashMap::new));

        // 退款趋势
        List<Map<String, Object>> refundRows;
        if ("WEEK".equals(granularity)) {
            refundRows = deltaStatisticsQueryMapper.selectRefundTrendByWeek(startTime, endTime);
        } else if ("MONTH".equals(granularity)) {
            refundRows = deltaStatisticsQueryMapper.selectRefundTrendByMonth(startTime, endTime);
        } else {
            refundRows = deltaStatisticsQueryMapper.selectRefundTrendByDay(startTime, endTime);
        }
        Map<String, Long> refundMap = refundRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"),
                        r -> toLong(r.get("refund_amount")), (a, b) -> a, LinkedHashMap::new));

        // 追回趋势
        List<Map<String, Object>> recoveryRows;
        if ("WEEK".equals(granularity)) {
            recoveryRows = deltaStatisticsQueryMapper.selectRecoveryTrendByWeek(startTime, endTime);
        } else if ("MONTH".equals(granularity)) {
            recoveryRows = deltaStatisticsQueryMapper.selectRecoveryTrendByMonth(startTime, endTime);
        } else {
            recoveryRows = deltaStatisticsQueryMapper.selectRecoveryTrendByDay(startTime, endTime);
        }
        Map<String, Long> recoveryMap = recoveryRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("date_val"),
                        r -> toLong(r.get("recovered_amount")), (a, b) -> a, LinkedHashMap::new));

        // 合并
        List<DeltaStatisticsTrendItemRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : orderRows) {
            String date = (String) row.get("date_val");
            DeltaStatisticsTrendItemRespVO item = new DeltaStatisticsTrendItemRespVO();
            item.setDate(date);
            item.setOrderCount(toLong(row.get("order_count")));
            item.setCompletedOrderCount(toLong(row.get("completed_count")));
            item.setServiceAmount(toLong(row.get("service_amount")));
            item.setCompletedServiceAmount(toLong(row.get("completed_amount")));
            item.setAfterSaleCount(afterSaleMap.getOrDefault(date, 0L));
            item.setRefundAmount(refundMap.getOrDefault(date, 0L));
            item.setRecoveredAmount(recoveryMap.getOrDefault(date, 0L));
            result.add(item);
        }
        return result;
    }

    // ====== 订单状态分布 ======

    @Override
    public List<DeltaStatisticsOrderStatusDistributionRespVO> getOrderStatusDistribution(
            DeltaStatisticsDateReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .between(DeltaServiceOrderDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));

        Map<Integer, List<DeltaServiceOrderDO>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() != null ? o.getStatus() : 0));

        List<DeltaStatisticsOrderStatusDistributionRespVO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<DeltaServiceOrderDO>> entry : grouped.entrySet()) {
            DeltaStatisticsOrderStatusDistributionRespVO vo = new DeltaStatisticsOrderStatusDistributionRespVO();
            vo.setStatus(entry.getKey());
            vo.setCount((long) entry.getValue().size());
            vo.setAmount(entry.getValue().stream()
                    .filter(o -> o.getServiceAmount() != null)
                    .mapToLong(DeltaServiceOrderDO::getServiceAmount).sum());
            // 状态名称
            Arrays.stream(ServiceOrderStatusEnum.values())
                    .filter(e -> e.getStatus().equals(entry.getKey()))
                    .findFirst()
                    .ifPresent(e -> vo.setStatusName(e.getName()));
            if (vo.getStatusName() == null) {
                vo.setStatusName("未知(" + entry.getKey() + ")");
            }
            result.add(vo);
        }
        return result;
    }

    // ====== 售后与资金统计 ======

    @Override
    public DeltaStatisticsAfterSaleSummaryRespVO getAfterSaleSummary(DeltaStatisticsDateReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        DeltaStatisticsAfterSaleSummaryRespVO vo = new DeltaStatisticsAfterSaleSummaryRespVO();

        List<DeltaAfterSaleDO> afterSales = deltaAfterSaleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaAfterSaleDO>()
                        .between(DeltaAfterSaleDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setAfterSaleCount((long) afterSales.size());
        vo.setPendingAfterSaleCount(afterSales.stream()
                .filter(a -> AfterSaleStatusEnum.PENDING.getStatus().equals(a.getStatus())).count());
        vo.setArbitratedAfterSaleCount(afterSales.stream()
                .filter(a -> AfterSaleStatusEnum.ARBITRATED.getStatus().equals(a.getStatus())).count());
        vo.setClosedAfterSaleCount(afterSales.stream()
                .filter(a -> AfterSaleStatusEnum.CLOSED.getStatus().equals(a.getStatus())).count());

        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .between(DeltaRefundRecordDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setRefundCount((long) refunds.size());
        vo.setPendingRefundCount(refunds.stream()
                .filter(r -> RefundStatusEnum.PENDING_MANUAL.getStatus().equals(r.getRefundStatus())).count());
        vo.setCompletedRefundCount(refunds.stream()
                .filter(r -> RefundStatusEnum.MANUAL_COMPLETED.getStatus().equals(r.getRefundStatus())).count());
        vo.setRefundAmount(refunds.stream().filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount()).sum());

        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .between(DeltaFundRecoveryDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime()));
        vo.setRecoveryTaskCount((long) recoveries.size());
        vo.setProcessingRecoveryCount(recoveries.stream()
                .filter(r -> RecoveryStatusEnum.PROCESSING.getStatus().equals(r.getRecoveryStatus())).count());
        vo.setCompletedRecoveryCount(recoveries.stream()
                .filter(r -> RecoveryStatusEnum.RECOVERED.getStatus().equals(r.getRecoveryStatus())).count());
        vo.setShouldRecoverAmount(recoveries.stream().filter(r -> r.getShouldRecoverAmount() != null)
                .mapToLong(r -> r.getShouldRecoverAmount()).sum());
        vo.setRecoveredAmount(recoveries.stream().filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount()).sum());
        vo.setRemainingRecoveryAmount(recoveries.stream().filter(r -> r.getRemainingAmount() != null)
                .mapToLong(r -> r.getRemainingAmount()).sum());

        return vo;
    }

    // ====== 打手排行 ======

    @Override
    public List<DeltaStatisticsWorkerRankingItemRespVO> getWorkerRanking(
            DeltaStatisticsWorkerRankingReqVO reqVO) {
        validateDateRange(reqVO.getStartTime(), reqVO.getEndTime());
        String rankingType = reqVO.getRankingType();
        String orderByClause;
        switch (rankingType) {
            case "COMPLETED_COUNT":
                orderByClause = "completed_count";
                break;
            case "SERVICE_AMOUNT":
                orderByClause = "service_amount";
                break;
            default:
                orderByClause = "order_count";
                break;
        }
        int limit = reqVO.getLimit(); // defaults to 10

        List<Map<String, Object>> rows = deltaStatisticsQueryMapper.selectWorkerRanking(
                reqVO.getStartTime(), reqVO.getEndTime(), orderByClause, limit);

        // 如果是结算排行，需要额外查询结算金额
        Map<Long, Long> settlementAmountMap = Collections.emptyMap();
        if ("SETTLEMENT_AMOUNT".equals(rankingType)) {
            List<Map<String, Object>> settlementRows = deltaStatisticsQueryMapper.selectWorkerSettlementAmount(
                    reqVO.getStartTime(), reqVO.getEndTime());
            settlementAmountMap = settlementRows.stream()
                    .collect(Collectors.toMap(
                            r -> toLong(r.get("worker_id")),
                            r -> toLong(r.get("settlement_amount"))));

            // 按结算金额重排
            rows = settlementRows.stream()
                    .sorted((a, b) -> Long.compare(toLong(b.get("settlement_amount")),
                            toLong(a.get("settlement_amount"))))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 收集workerIds并批量查询打手名称
        Set<Long> workerIds = rows.stream()
                .map(r -> toLong(r.get("worker_id")))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> workerNameMap = new HashMap<>();
        for (Long workerId : workerIds) {
            DeltaWorkerDO worker = deltaWorkerService.getWorker(workerId);
            if (worker != null) {
                workerNameMap.put(workerId,
                        worker.getDisplayName() != null ? worker.getDisplayName() : worker.getRealName());
            }
        }

        List<DeltaStatisticsWorkerRankingItemRespVO> result = new ArrayList<>();
        int rank = 0;
        for (Map<String, Object> row : rows) {
            DeltaStatisticsWorkerRankingItemRespVO item = new DeltaStatisticsWorkerRankingItemRespVO();
            Long workerId = toLong(row.get("worker_id"));
            item.setWorkerId(workerId);
            item.setWorkerName(workerNameMap.getOrDefault(workerId, "打手" + workerId));
            item.setOrderCount(toLong(row.get("order_count")));
            item.setCompletedCount(toLong(row.get("completed_count")));
            item.setServiceAmount(toLong(row.get("service_amount")));
            item.setSettlementAmount(settlementAmountMap.getOrDefault(workerId,
                    toLong(row.get("settlement_amount"))));
            item.setRank(++rank);
            result.add(item);
        }
        return result;
    }

    // ====== 辅助方法 ======

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
