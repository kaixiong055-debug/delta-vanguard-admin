package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;

import java.util.List;

/**
 * 派单记录 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderAssignmentService {

    /**
     * 根据服务单ID查询有效分配记录
     */
    DeltaOrderAssignmentDO getActiveAssignmentByServiceOrderId(Long serviceOrderId);

    /**
     * 根据打手ID查询有效分配记录列表
     */
    List<DeltaOrderAssignmentDO> getActiveAssignmentsByWorkerId(Long workerId);

    /**
     * 根据服务单ID查询所有分配历史
     */
    List<DeltaOrderAssignmentDO> getAssignmentHistory(Long serviceOrderId);

    /**
     * 创建分配记录
     */
    DeltaOrderAssignmentDO createAssignment(DeltaOrderAssignmentDO assignment);

    /**
     * 取消分配记录
     */
    void cancelAssignment(Long assignmentId, String reason);

    /**
     * 根据ID查询
     */
    DeltaOrderAssignmentDO getAssignment(Long id);

}
