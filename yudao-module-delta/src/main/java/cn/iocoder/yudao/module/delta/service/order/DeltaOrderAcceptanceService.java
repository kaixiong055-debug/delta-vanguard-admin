package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAcceptanceDO;

import java.util.List;

/**
 * 验收记录 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderAcceptanceService {

    /**
     * 创建验收记录
     */
    DeltaOrderAcceptanceDO createAcceptance(DeltaOrderAcceptanceDO acceptance);

    /**
     * 根据服务单ID查询验收记录列表
     */
    List<DeltaOrderAcceptanceDO> getAcceptanceListByServiceOrderId(Long serviceOrderId);

    /**
     * 根据服务单ID批量查询验收记录
     */
    List<DeltaOrderAcceptanceDO> getAcceptanceListByServiceOrderIds(List<Long> serviceOrderIds);

}
