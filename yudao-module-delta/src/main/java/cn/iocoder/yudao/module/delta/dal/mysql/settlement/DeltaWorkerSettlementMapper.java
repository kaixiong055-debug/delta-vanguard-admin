package cn.iocoder.yudao.module.delta.dal.mysql.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.settlement.vo.DeltaWorkerSettlementPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打手结算 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerSettlementMapper extends BaseMapperX<DeltaWorkerSettlementDO> {

    default DeltaWorkerSettlementDO selectByServiceOrderId(Long serviceOrderId) {
        return selectOne(DeltaWorkerSettlementDO::getServiceOrderId, serviceOrderId);
    }

    /**
     * 后台分页查询结算列表
     */
    default PageResult<DeltaWorkerSettlementDO> selectPage(DeltaWorkerSettlementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eqIfPresent(DeltaWorkerSettlementDO::getSettlementNo, reqVO.getSettlementNo())
                .eqIfPresent(DeltaWorkerSettlementDO::getServiceOrderId, reqVO.getServiceOrderId())
                .eqIfPresent(DeltaWorkerSettlementDO::getWorkerId, reqVO.getWorkerId())
                .eqIfPresent(DeltaWorkerSettlementDO::getSettlementStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaWorkerSettlementDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaWorkerSettlementDO::getId));
    }

    /**
     * App 打手分页查询自己的结算
     */
    default PageResult<DeltaWorkerSettlementDO> selectPageByWorker(Long workerId, Integer status,
                                                                    cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId)
                .eqIfPresent(DeltaWorkerSettlementDO::getSettlementStatus, status)
                .orderByDesc(DeltaWorkerSettlementDO::getId));
    }

    /**
     * CAS 更新结算状态
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer oldStatus,
                                 java.util.function.Consumer<LambdaUpdateWrapper<DeltaWorkerSettlementDO>> extraUpdater) {
        LambdaUpdateWrapper<DeltaWorkerSettlementDO> wrapper = new LambdaUpdateWrapper<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getId, id)
                .eq(DeltaWorkerSettlementDO::getSettlementStatus, oldStatus);
        if (extraUpdater != null) {
            extraUpdater.accept(wrapper);
        }
        wrapper.set(DeltaWorkerSettlementDO::getSettlementStatus, newStatus);
        return update(null, wrapper);
    }

    /**
     * 汇总打手结算金额（按状态）
     */
    default java.util.Map<String, Object> selectSummaryByWorker(Long workerId) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        // 待审核金额
        Long pendingReview = selectCount(new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId)
                .eq(DeltaWorkerSettlementDO::getSettlementStatus, 0));
        // 审核通过金额
        Long approved = selectCount(new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId)
                .eq(DeltaWorkerSettlementDO::getSettlementStatus, 1));
        // 已打款金额
        Long paid = selectCount(new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId)
                .eq(DeltaWorkerSettlementDO::getSettlementStatus, 3));
        // 审核驳回金额
        Long rejected = selectCount(new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId)
                .eq(DeltaWorkerSettlementDO::getSettlementStatus, 2));

        result.put("pendingReviewCount", pendingReview);
        result.put("approvedCount", approved);
        result.put("paidCount", paid);
        result.put("rejectedCount", rejected);
        return result;
    }

    /**
     * 查询打手待审核/审核通过/已打款的结算金额总和（用于 App 汇总金额展示）
     */
    default java.util.Map<String, Object> selectAmountSummaryByWorker(Long workerId) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        java.util.List<DeltaWorkerSettlementDO> allSettlements = selectList(new LambdaQueryWrapperX<DeltaWorkerSettlementDO>()
                .eq(DeltaWorkerSettlementDO::getWorkerId, workerId));

        long pendingReviewAmount = 0L;
        long approvedAmount = 0L;
        long paidAmount = 0L;
        long rejectedAmount = 0L;

        for (DeltaWorkerSettlementDO s : allSettlements) {
            int amt = (s.getWorkerAmount() != null) ? s.getWorkerAmount() : 0;
            int status = (s.getSettlementStatus() != null) ? s.getSettlementStatus() : 0;
            if (status == 0) pendingReviewAmount += amt;
            else if (status == 1) approvedAmount += amt;
            else if (status == 3) paidAmount += amt;
            else if (status == 2) rejectedAmount += amt;
        }

        result.put("pendingReviewAmount", pendingReviewAmount);
        result.put("approvedAmount", approvedAmount);
        result.put("paidAmount", paidAmount);
        result.put("rejectedAmount", rejectedAmount);
        result.put("totalPaidAmount", paidAmount);
        return result;
    }

}
