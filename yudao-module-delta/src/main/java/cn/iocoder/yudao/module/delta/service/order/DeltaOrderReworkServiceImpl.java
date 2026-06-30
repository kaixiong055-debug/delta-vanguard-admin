package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderReworkDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderReworkMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 返工记录 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
public class DeltaOrderReworkServiceImpl implements DeltaOrderReworkService {

    @Resource
    private DeltaOrderReworkMapper deltaOrderReworkMapper;

    @Override
    public DeltaOrderReworkDO createRework(DeltaOrderReworkDO rework) {
        deltaOrderReworkMapper.insert(rework);
        return rework;
    }

    @Override
    public List<DeltaOrderReworkDO> getReworkListByServiceOrderId(Long serviceOrderId) {
        return deltaOrderReworkMapper.selectListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderReworkDO> getReworkListByServiceOrderIds(List<Long> serviceOrderIds) {
        return deltaOrderReworkMapper.selectListByServiceOrderIds(serviceOrderIds);
    }

    @Override
    public long countReworkByServiceOrderId(Long serviceOrderId) {
        return deltaOrderReworkMapper.selectCountByServiceOrderId(serviceOrderId);
    }

}
