package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;

/**
 * 打手结算 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaWorkerSettlementService {

    /**
     * 根据ID查询结算记录
     */
    DeltaWorkerSettlementDO getSettlement(Long id);

    /**
     * 根据服务订单ID查询结算记录（幂等校验用）
     */
    DeltaWorkerSettlementDO getSettlementByServiceOrderId(Long serviceOrderId);

    /**
     * 创建结算记录
     */
    void createSettlement(DeltaWorkerSettlementDO settlement);

    /**
     * CAS 更新结算记录
     */
    int updateByIdCas(DeltaWorkerSettlementDO settlement);

    /**
     * App 打手分页查询结算
     */
    PageResult<DeltaWorkerSettlementDO> getSettlementPageByWorker(Long workerId, Integer status,
                                                                   cn.iocoder.yudao.framework.common.pojo.PageParam pageParam);

    /**
     * 汇总打手结算金额
     */
    java.util.Map<String, Object> getAmountSummaryByWorker(Long workerId);

    /**
     * 获取 Mapper（供 Core Service 使用）
     */
    cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper getMapper();

}
