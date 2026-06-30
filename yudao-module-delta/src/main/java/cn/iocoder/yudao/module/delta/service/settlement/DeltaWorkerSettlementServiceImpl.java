package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 打手结算 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaWorkerSettlementServiceImpl implements DeltaWorkerSettlementService {

    @Resource
    private DeltaWorkerSettlementMapper deltaWorkerSettlementMapper;

    @Override
    public DeltaWorkerSettlementDO getSettlement(Long id) {
        return deltaWorkerSettlementMapper.selectById(id);
    }

    @Override
    public DeltaWorkerSettlementDO getSettlementByServiceOrderId(Long serviceOrderId) {
        return deltaWorkerSettlementMapper.selectByServiceOrderId(serviceOrderId);
    }

    @Override
    public void createSettlement(DeltaWorkerSettlementDO settlement) {
        deltaWorkerSettlementMapper.insert(settlement);
    }

    @Override
    public int updateByIdCas(DeltaWorkerSettlementDO settlement) {
        return deltaWorkerSettlementMapper.updateById(settlement);
    }

    @Override
    public PageResult<DeltaWorkerSettlementDO> getSettlementPageByWorker(Long workerId, Integer status,
                                                                          cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return deltaWorkerSettlementMapper.selectPageByWorker(workerId, status, pageParam);
    }

    @Override
    public java.util.Map<String, Object> getAmountSummaryByWorker(Long workerId) {
        return deltaWorkerSettlementMapper.selectAmountSummaryByWorker(workerId);
    }

    @Override
    public DeltaWorkerSettlementMapper getMapper() {
        return deltaWorkerSettlementMapper;
    }

}
