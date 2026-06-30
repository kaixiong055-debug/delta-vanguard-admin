package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAcceptanceDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAcceptanceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 验收记录 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
public class DeltaOrderAcceptanceServiceImpl implements DeltaOrderAcceptanceService {

    @Resource
    private DeltaOrderAcceptanceMapper deltaOrderAcceptanceMapper;

    @Override
    public DeltaOrderAcceptanceDO createAcceptance(DeltaOrderAcceptanceDO acceptance) {
        deltaOrderAcceptanceMapper.insert(acceptance);
        return acceptance;
    }

    @Override
    public List<DeltaOrderAcceptanceDO> getAcceptanceListByServiceOrderId(Long serviceOrderId) {
        return deltaOrderAcceptanceMapper.selectListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderAcceptanceDO> getAcceptanceListByServiceOrderIds(List<Long> serviceOrderIds) {
        return deltaOrderAcceptanceMapper.selectListByServiceOrderIds(serviceOrderIds);
    }

}
