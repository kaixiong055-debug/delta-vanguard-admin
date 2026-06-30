package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单日志 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaOrderLogServiceImpl implements DeltaOrderLogService {

    @Resource
    private DeltaOrderLogMapper deltaOrderLogMapper;

    @Override
    public void createOrderLog(DeltaOrderLogDO log) {
        deltaOrderLogMapper.insert(log);
    }

    @Override
    public List<DeltaOrderLogDO> getLogsByServiceOrderId(Long serviceOrderId) {
        return deltaOrderLogMapper.selectListByServiceOrderId(serviceOrderId);
    }

}
