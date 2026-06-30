package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaFundRecoveryPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.function.Consumer;

/**
 * 追回任务 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaFundRecoveryMapper extends BaseMapperX<DeltaFundRecoveryDO> {

    default DeltaFundRecoveryDO selectByArbitrationId(Long arbitrationId) {
        return selectOne(DeltaFundRecoveryDO::getArbitrationId, arbitrationId);
    }

    default List<DeltaFundRecoveryDO> selectListBySettlementId(Long settlementId) {
        return selectList(new LambdaQueryWrapperX<DeltaFundRecoveryDO>()
                .eq(DeltaFundRecoveryDO::getSettlementId, settlementId)
                .orderByDesc(DeltaFundRecoveryDO::getId));
    }

    default List<DeltaFundRecoveryDO> selectListByAfterSaleId(Long afterSaleId) {
        return selectList(new LambdaQueryWrapperX<DeltaFundRecoveryDO>()
                .eq(DeltaFundRecoveryDO::getAfterSaleId, afterSaleId)
                .orderByDesc(DeltaFundRecoveryDO::getId));
    }

    default PageResult<DeltaFundRecoveryDO> selectPage(DeltaFundRecoveryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaFundRecoveryDO>()
                .eqIfPresent(DeltaFundRecoveryDO::getRecoveryNo, reqVO.getRecoveryNo())
                .eqIfPresent(DeltaFundRecoveryDO::getWorkerId, reqVO.getWorkerId())
                .eqIfPresent(DeltaFundRecoveryDO::getRecoveryStatus, reqVO.getRecoveryStatus())
                .eqIfPresent(DeltaFundRecoveryDO::getHandlerId, reqVO.getHandlerId())
                .betweenIfPresent(DeltaFundRecoveryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaFundRecoveryDO::getId));
    }

    /**
     * CAS 更新追回状态
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer expectedStatus,
                                 Consumer<LambdaUpdateWrapper<DeltaFundRecoveryDO>> extra) {
        LambdaUpdateWrapper<DeltaFundRecoveryDO> wrapper = new LambdaUpdateWrapper<DeltaFundRecoveryDO>()
                .eq(DeltaFundRecoveryDO::getId, id)
                .eq(DeltaFundRecoveryDO::getRecoveryStatus, expectedStatus)
                .set(DeltaFundRecoveryDO::getRecoveryStatus, newStatus);
        extra.accept(wrapper);
        return update(null, wrapper);
    }

    /**
     * CAS 更新追回状态和金额
     */
    default int updateStatusCasWithAmount(Long id, Integer newStatus, Integer expectedStatus,
                                           Integer recoveredAmount, Integer remainingAmount,
                                           Consumer<LambdaUpdateWrapper<DeltaFundRecoveryDO>> extra) {
        LambdaUpdateWrapper<DeltaFundRecoveryDO> wrapper = new LambdaUpdateWrapper<DeltaFundRecoveryDO>()
                .eq(DeltaFundRecoveryDO::getId, id)
                .eq(DeltaFundRecoveryDO::getRecoveryStatus, expectedStatus)
                .set(DeltaFundRecoveryDO::getRecoveryStatus, newStatus)
                .set(DeltaFundRecoveryDO::getRecoveredAmount, recoveredAmount)
                .set(DeltaFundRecoveryDO::getRemainingAmount, remainingAmount);
        extra.accept(wrapper);
        return update(null, wrapper);
    }

}
