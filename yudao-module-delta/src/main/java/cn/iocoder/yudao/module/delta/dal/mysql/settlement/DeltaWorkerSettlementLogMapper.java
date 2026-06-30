package cn.iocoder.yudao.module.delta.dal.mysql.settlement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 结算操作日志 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerSettlementLogMapper extends BaseMapperX<DeltaWorkerSettlementLogDO> {

    default List<DeltaWorkerSettlementLogDO> selectListBySettlementId(Long settlementId) {
        return selectList(new LambdaQueryWrapperX<DeltaWorkerSettlementLogDO>()
                .eq(DeltaWorkerSettlementLogDO::getSettlementId, settlementId)
                .orderByAsc(DeltaWorkerSettlementLogDO::getCreateTime));
    }

}
