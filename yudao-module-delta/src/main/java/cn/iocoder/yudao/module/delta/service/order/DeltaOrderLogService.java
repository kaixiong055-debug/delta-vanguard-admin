package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;

import java.util.List;

/**
 * 订单日志 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderLogService {

    /**
     * 创建订单日志
     */
    void createOrderLog(DeltaOrderLogDO log);

    /**
     * 根据服务单ID查询日志列表
     */
    List<DeltaOrderLogDO> getLogsByServiceOrderId(Long serviceOrderId);

}
