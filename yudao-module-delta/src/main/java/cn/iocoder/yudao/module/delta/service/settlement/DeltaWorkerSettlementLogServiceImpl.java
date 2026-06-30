package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 结算操作日志 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaWorkerSettlementLogServiceImpl implements DeltaWorkerSettlementLogService {

    @Resource
    private DeltaWorkerSettlementLogMapper deltaWorkerSettlementLogMapper;

    @Override
    public void createLog(DeltaWorkerSettlementLogDO log) {
        deltaWorkerSettlementLogMapper.insert(log);
    }

    @Override
    public List<DeltaWorkerSettlementLogDO> getLogsBySettlementId(Long settlementId) {
        return deltaWorkerSettlementLogMapper.selectListBySettlementId(settlementId);
    }

}
