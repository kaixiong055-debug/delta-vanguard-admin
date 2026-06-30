package cn.iocoder.yudao.module.delta.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.delta.convert.finance.DeltaFinanceReconciliationConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.finance.DeltaFinanceReconciliationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.mysql.finance.DeltaFinanceReconciliationMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaFundRecoveryMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaRefundRecordMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.delta.enums.finance.DeltaFinanceReconciliationStatusEnum;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 财务对账 Service 实现
 */
@Service
@Validated
@Slf4j
public class DeltaFinanceReconciliationServiceImpl implements DeltaFinanceReconciliationService {

    @Resource
    private DeltaFinanceReconciliationMapper deltaFinanceReconciliationMapper;
    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;
    @Resource
    private DeltaRefundRecordMapper deltaRefundRecordMapper;
    @Resource
    private DeltaFundRecoveryMapper deltaFundRecoveryMapper;

    // ====== 分页 ======

    @Override
    public PageResult<DeltaFinanceReconciliationRespVO> getReconciliationPage(
            DeltaFinanceReconciliationPageReqVO reqVO) {
        PageResult<DeltaFinanceReconciliationDO> pageResult = deltaFinanceReconciliationMapper.selectPage(reqVO);
        List<DeltaFinanceReconciliationRespVO> voList = DeltaFinanceReconciliationConvert.INSTANCE
                .convertList(pageResult.getList());
        DeltaFinanceReconciliationConvert.INSTANCE.fillStatusName(voList);
        return new PageResult<>(voList, pageResult.getTotal());
    }

    // ====== 详情 ======

    @Override
    public DeltaFinanceReconciliationRespVO getReconciliation(Long id) {
        DeltaFinanceReconciliationDO reconciliation = validateExists(id);
        DeltaFinanceReconciliationRespVO vo = DeltaFinanceReconciliationConvert.INSTANCE.convert(reconciliation);
        DeltaFinanceReconciliationConvert.INSTANCE.fillStatusName(vo);
        return vo;
    }

    // ====== 生成对账 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateReconciliation(@Valid DeltaFinanceReconciliationGenerateReqVO reqVO) {
        LocalDate reconciliationDate = reqVO.getReconciliationDate();
        LocalDateTime periodStart = reconciliationDate.atStartOfDay();
        LocalDateTime periodEnd = reconciliationDate.plusDays(1).atStartOfDay();

        // 预检查：同一租户同一天不能重复
        DeltaFinanceReconciliationDO existing = deltaFinanceReconciliationMapper
                .selectByReconciliationDate(reconciliationDate);
        if (existing != null) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_DUPLICATE);
        }

        // 生成对账单号
        String reconciliationNo = generateReconciliationNo(reconciliationDate);

        // 构建 DO
        DeltaFinanceReconciliationDO reconciliation = DeltaFinanceReconciliationDO.builder()
                .reconciliationNo(reconciliationNo)
                .reconciliationDate(reconciliationDate)
                .periodStartTime(periodStart)
                .periodEndTime(periodEnd)
                .status(DeltaFinanceReconciliationStatusEnum.PENDING.getStatus())
                .version(0)
                .build();

        // 计算各汇总指标
        calculateMetrics(reconciliation, periodStart, periodEnd);

        reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.PENDING.getStatus());
        reconciliation.setCalculateTime(LocalDateTime.now());

        // 判断对账结果
        long diff = (reconciliation.getActualPlatformAmount() != null ? reconciliation.getActualPlatformAmount() : 0L)
                - (reconciliation.getExpectedPlatformAmount() != null ? reconciliation.getExpectedPlatformAmount() : 0L);
        reconciliation.setDifferenceAmount(diff);
        if (diff == 0) {
            reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.MATCHED.getStatus());
        } else {
            reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.DIFFERENCE.getStatus());
        }

        // 插入
        deltaFinanceReconciliationMapper.insert(reconciliation);
        log.info("生成对账单 {} - 日期 {} - 状态 {} - 差异 {}",
                reconciliationNo, reconciliationDate, reconciliation.getStatus(), diff);

        return reconciliation.getId();
    }

    /**
     * 计算对账周期内的各汇总指标
     */
    private void calculateMetrics(DeltaFinanceReconciliationDO reconciliation,
                                  LocalDateTime periodStart, LocalDateTime periodEnd) {
        // 1. 服务订单汇总
        List<DeltaServiceOrderDO> orders = deltaServiceOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaServiceOrderDO>()
                        .between(DeltaServiceOrderDO::getCreateTime, periodStart, periodEnd));
        reconciliation.setServiceOrderCount(orders.size());
        reconciliation.setServiceOrderAmount(orders.stream()
                .filter(o -> o.getServiceAmount() != null)
                .mapToLong(o -> o.getServiceAmount().longValue()).sum());

        // 2. 结算汇总
        List<DeltaWorkerSettlementDO> settlements = deltaWorkerSettlementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaWorkerSettlementDO>()
                        .between(DeltaWorkerSettlementDO::getCreateTime, periodStart, periodEnd));
        reconciliation.setSettlementCount(settlements.size());
        reconciliation.setSettlementAmount(settlements.stream()
                .filter(s -> s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount().longValue()).sum());
        reconciliation.setPaidSettlementAmount(settlements.stream()
                .filter(s -> s.getSettlementStatus() != null && s.getSettlementStatus() == 3
                        && s.getWorkerAmount() != null)
                .mapToLong(s -> s.getWorkerAmount().longValue()).sum());

        // 3. 退款汇总
        List<DeltaRefundRecordDO> refunds = deltaRefundRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaRefundRecordDO>()
                        .between(DeltaRefundRecordDO::getCreateTime, periodStart, periodEnd));
        reconciliation.setRefundCount(refunds.size());
        reconciliation.setRefundAmount(refunds.stream()
                .filter(r -> r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount().longValue()).sum());

        // 4. 追回汇总
        List<DeltaFundRecoveryDO> recoveries = deltaFundRecoveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeltaFundRecoveryDO>()
                        .between(DeltaFundRecoveryDO::getCreateTime, periodStart, periodEnd));
        reconciliation.setRecoveryCount(recoveries.size());
        reconciliation.setShouldRecoverAmount(recoveries.stream()
                .filter(r -> r.getShouldRecoverAmount() != null)
                .mapToLong(r -> r.getShouldRecoverAmount().longValue()).sum());
        reconciliation.setRecoveredAmount(recoveries.stream()
                .filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount().longValue()).sum());

        // 5. 平台金额计算
        // expectedPlatformAmount = 服务订单平台佣金 - 退款金额(仅限已完成的退款)
        // actualPlatformAmount = 服务订单平台佣金(有platform_fee字段)
        long platformFeeTotal = orders.stream()
                .filter(o -> o.getPlatformFee() != null)
                .mapToLong(o -> o.getPlatformFee().longValue()).sum();
        long completedRefundAmount = refunds.stream()
                .filter(r -> r.getRefundStatus() != null && r.getRefundStatus() == 2
                        && r.getRefundAmount() != null)
                .mapToLong(r -> r.getRefundAmount().longValue()).sum();
        long recoveredAmount = recoveries.stream()
                .filter(r -> r.getRecoveredAmount() != null)
                .mapToLong(r -> r.getRecoveredAmount().longValue()).sum();

        reconciliation.setExpectedPlatformAmount(platformFeeTotal - completedRefundAmount + recoveredAmount);
        reconciliation.setActualPlatformAmount(platformFeeTotal);
    }

    // ====== 确认对账 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReconciliation(@Valid DeltaFinanceReconciliationConfirmReqVO reqVO) {
        DeltaFinanceReconciliationDO reconciliation = validateExists(reqVO.getId());

        // 状态检查
        if (!DeltaFinanceReconciliationStatusEnum.canConfirm(reconciliation.getStatus())) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED);
        }
        if (DeltaFinanceReconciliationStatusEnum.isFinalStatus(reconciliation.getStatus())) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_ALREADY_CONFIRMED);
        }

        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        int rows = deltaFinanceReconciliationMapper.updateStatusCas(
                reqVO.getId(),
                DeltaFinanceReconciliationStatusEnum.CONFIRMED.getStatus(),
                reconciliation.getStatus(),
                reconciliation.getVersion(),
                wrapper -> {
                    wrapper.set(DeltaFinanceReconciliationDO::getConfirmRemark,
                            reqVO.getRemark() != null ? reqVO.getRemark() : "");
                    wrapper.set(DeltaFinanceReconciliationDO::getConfirmerId, adminUserId);
                    wrapper.set(DeltaFinanceReconciliationDO::getConfirmedTime, LocalDateTime.now());
                });

        if (rows == 0) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED);
        }
        log.info("确认对账 {} - 确认人 {}", reconciliation.getReconciliationNo(), adminUserId);
    }

    // ====== 重试 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryReconciliation(Long id) {
        DeltaFinanceReconciliationDO reconciliation = validateExists(id);

        if (!DeltaFinanceReconciliationStatusEnum.canRetry(reconciliation.getStatus())) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED);
        }

        // 重新计算
        try {
            calculateMetrics(reconciliation, reconciliation.getPeriodStartTime(),
                    reconciliation.getPeriodEndTime());

            long diff = (reconciliation.getActualPlatformAmount() != null
                    ? reconciliation.getActualPlatformAmount() : 0L)
                    - (reconciliation.getExpectedPlatformAmount() != null
                    ? reconciliation.getExpectedPlatformAmount() : 0L);
            reconciliation.setDifferenceAmount(diff);
            if (diff == 0) {
                reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.MATCHED.getStatus());
            } else {
                reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.DIFFERENCE.getStatus());
            }
            reconciliation.setFailureReason(null);
            reconciliation.setCalculateTime(LocalDateTime.now());

            deltaFinanceReconciliationMapper.updateById(reconciliation);
            log.info("重试对账成功 {} - 状态 {}", reconciliation.getReconciliationNo(),
                    reconciliation.getStatus());
        } catch (Exception e) {
            log.error("重试对账失败 {}", reconciliation.getReconciliationNo(), e);
            reconciliation.setStatus(DeltaFinanceReconciliationStatusEnum.FAILED.getStatus());
            reconciliation.setFailureReason("重试计算异常: " + e.getMessage());
            deltaFinanceReconciliationMapper.updateById(reconciliation);
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_CALCULATE_FAILED);
        }
    }

    // ====== 取消 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReconciliation(@Valid DeltaFinanceReconciliationCancelReqVO reqVO) {
        DeltaFinanceReconciliationDO reconciliation = validateExists(reqVO.getId());

        if (!DeltaFinanceReconciliationStatusEnum.canCancel(reconciliation.getStatus())) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED);
        }
        if (DeltaFinanceReconciliationStatusEnum.isFinalStatus(reconciliation.getStatus())) {
            throw exception(DeltaFinanceReconciliationStatusEnum.isConfirmed(reconciliation.getStatus())
                    ? ErrorCodeConstants.FINANCE_RECONCILIATION_ALREADY_CONFIRMED
                    : ErrorCodeConstants.FINANCE_RECONCILIATION_ALREADY_CANCELED);
        }

        int rows = deltaFinanceReconciliationMapper.updateStatusCas(
                reqVO.getId(),
                DeltaFinanceReconciliationStatusEnum.CANCELED.getStatus(),
                reconciliation.getStatus(),
                reconciliation.getVersion(),
                wrapper -> {
                    wrapper.set(DeltaFinanceReconciliationDO::getFailureReason, reqVO.getReason());
                });

        if (rows == 0) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED);
        }
        log.info("取消对账 {} - 原因 {}", reconciliation.getReconciliationNo(), reqVO.getReason());
    }

    // ====== 辅助 ======

    private DeltaFinanceReconciliationDO validateExists(Long id) {
        DeltaFinanceReconciliationDO reconciliation = deltaFinanceReconciliationMapper.selectById(id);
        if (reconciliation == null) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_NOT_EXISTS);
        }
        return reconciliation;
    }

    private String generateReconciliationNo(LocalDate date) {
        return "REC" + date.toString().replace("-", "");
    }

    // ====== 导出 ======

    /** 最大导出记录数 */
    private static final int MAX_EXPORT_COUNT = 10000;

    @Override
    public List<DeltaFinanceReconciliationRespVO> getReconciliationList(
            @Valid DeltaFinanceReconciliationExportReqVO reqVO) {
        List<DeltaFinanceReconciliationDO> list = deltaFinanceReconciliationMapper.selectListForExport(
                reqVO.getReconciliationNo(),
                reqVO.getReconciliationDate(),
                reqVO.getStatus(),
                reqVO.getCreateTime(),
                MAX_EXPORT_COUNT + 1);
        if (list.size() > MAX_EXPORT_COUNT) {
            throw exception(ErrorCodeConstants.FINANCE_RECONCILIATION_EXPORT_OVER_LIMIT);
        }
        List<DeltaFinanceReconciliationRespVO> voList = DeltaFinanceReconciliationConvert.INSTANCE
                .convertList(list);
        DeltaFinanceReconciliationConvert.INSTANCE.fillStatusName(voList);
        return voList;
    }

}
