package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderEvidenceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务凭证 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaOrderEvidenceServiceImpl implements DeltaOrderEvidenceService {

    @Resource
    private DeltaOrderEvidenceMapper deltaOrderEvidenceMapper;

    @Override
    public DeltaOrderEvidenceDO createEvidence(DeltaOrderEvidenceDO evidence) {
        deltaOrderEvidenceMapper.insert(evidence);
        return evidence;
    }

    @Override
    public DeltaOrderEvidenceDO getEvidence(Long id) {
        return deltaOrderEvidenceMapper.selectById(id);
    }

    @Override
    public void deleteEvidence(Long id) {
        deltaOrderEvidenceMapper.deleteById(id);
    }

    @Override
    public List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderId(Long serviceOrderId) {
        return deltaOrderEvidenceMapper.selectListByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId) {
        return deltaOrderEvidenceMapper.selectListByServiceOrderIdAndWorkerId(serviceOrderId, workerId);
    }

    @Override
    public List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderIds(List<Long> serviceOrderIds) {
        return deltaOrderEvidenceMapper.selectListByServiceOrderIds(serviceOrderIds);
    }

    @Override
    public long countEvidenceByServiceOrderId(Long serviceOrderId) {
        return deltaOrderEvidenceMapper.selectCountByServiceOrderId(serviceOrderId);
    }

}
