package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;

import java.util.List;

/**
 * 服务凭证 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderEvidenceService {

    /**
     * 创建凭证记录
     */
    DeltaOrderEvidenceDO createEvidence(DeltaOrderEvidenceDO evidence);

    /**
     * 根据ID查询凭证
     */
    DeltaOrderEvidenceDO getEvidence(Long id);

    /**
     * 逻辑删除凭证
     */
    void deleteEvidence(Long id);

    /**
     * 根据服务单ID查询有效凭证列表
     */
    List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderId(Long serviceOrderId);

    /**
     * 根据服务单ID和打手ID查询凭证列表
     */
    List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId);

    /**
     * 根据服务单ID列表批量查询凭证
     */
    List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderIds(List<Long> serviceOrderIds);

    /**
     * 统计服务单有效凭证数量
     */
    long countEvidenceByServiceOrderId(Long serviceOrderId);

}
