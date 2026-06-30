package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderProgressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务进度 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaOrderProgressServiceImpl implements DeltaOrderProgressService {

    @Resource
    private DeltaOrderProgressMapper deltaOrderProgressMapper;

    @Override
    public DeltaOrderProgressDO createProgress(DeltaOrderProgressDO progress) {
        deltaOrderProgressMapper.insert(progress);
        return progress;
    }

    @Override
    public List<DeltaOrderProgressDO> getProgressListByServiceOrderId(Long serviceOrderId) {
        return deltaOrderProgressMapper.selectListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderProgressDO> getProgressListByServiceOrderIds(List<Long> serviceOrderIds) {
        return deltaOrderProgressMapper.selectListByServiceOrderIds(serviceOrderIds);
    }

    @Override
    public List<DeltaOrderProgressDO> getProgressListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId) {
        return deltaOrderProgressMapper.selectListByServiceOrderIdAndWorkerId(serviceOrderId, workerId);
    }

}
