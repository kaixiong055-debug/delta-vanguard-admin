package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;

import java.util.List;

/**
 * 服务进度 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderProgressService {

    /**
     * 创建进度记录
     */
    DeltaOrderProgressDO createProgress(DeltaOrderProgressDO progress);

    /**
     * 根据服务单ID查询进度列表（按创建时间正序）
     */
    List<DeltaOrderProgressDO> getProgressListByServiceOrderId(Long serviceOrderId);

    /**
     * 根据服务单ID列表批量查询进度
     */
    List<DeltaOrderProgressDO> getProgressListByServiceOrderIds(List<Long> serviceOrderIds);

    /**
     * 根据服务单ID和打手ID查询进度列表
     */
    List<DeltaOrderProgressDO> getProgressListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId);

}
