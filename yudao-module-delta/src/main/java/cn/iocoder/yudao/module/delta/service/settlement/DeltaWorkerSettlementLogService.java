package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;

import java.util.List;

/**
 * 结算操作日志 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaWorkerSettlementLogService {

    /**
     * 创建结算操作日志
     */
    void createLog(DeltaWorkerSettlementLogDO log);

    /**
     * 根据结算ID查询日志列表
     */
    List<DeltaWorkerSettlementLogDO> getLogsBySettlementId(Long settlementId);

}
