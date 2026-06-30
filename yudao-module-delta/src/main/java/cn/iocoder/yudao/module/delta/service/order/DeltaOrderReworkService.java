package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderReworkDO;

import java.util.List;

/**
 * 返工记录 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderReworkService {

    /**
     * 创建返工记录
     */
    DeltaOrderReworkDO createRework(DeltaOrderReworkDO rework);

    /**
     * 根据服务单ID查询返工记录列表
     */
    List<DeltaOrderReworkDO> getReworkListByServiceOrderId(Long serviceOrderId);

    /**
     * 根据服务单ID批量查询返工记录
     */
    List<DeltaOrderReworkDO> getReworkListByServiceOrderIds(List<Long> serviceOrderIds);

    /**
     * 统计服务单返工次数
     */
    long countReworkByServiceOrderId(Long serviceOrderId);

}
