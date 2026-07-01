package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 派单记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderAssignmentMapper extends BaseMapperX<DeltaOrderAssignmentDO> {

    /**
     * 根据服务单ID查询有效分配记录（已接受状态）
     */
    default DeltaOrderAssignmentDO selectActiveByServiceOrderId(Long serviceOrderId) {
        return selectOne(new LambdaQueryWrapper<DeltaOrderAssignmentDO>()
                .eq(DeltaOrderAssignmentDO::getServiceOrderId, serviceOrderId)
                .eq(DeltaOrderAssignmentDO::getAssignmentStatus, AssignmentStatusEnum.ACCEPTED.getStatus()));
    }

    /** 查询待确认或已接受的有效派单记录。 */
    default DeltaOrderAssignmentDO selectAnyActiveByServiceOrderId(Long serviceOrderId) {
        return selectOne(new LambdaQueryWrapper<DeltaOrderAssignmentDO>()
                .eq(DeltaOrderAssignmentDO::getServiceOrderId, serviceOrderId)
                .in(DeltaOrderAssignmentDO::getAssignmentStatus,
                        AssignmentStatusEnum.PENDING.getStatus(),
                        AssignmentStatusEnum.ACCEPTED.getStatus())
                .last("LIMIT 1"));
    }

    /**
     * 根据打手ID查询有效分配记录列表（已接受状态）
     */
    default List<DeltaOrderAssignmentDO> selectActiveListByWorkerId(Long workerId) {
        return selectList(new LambdaQueryWrapper<DeltaOrderAssignmentDO>()
                .eq(DeltaOrderAssignmentDO::getWorkerId, workerId)
                .eq(DeltaOrderAssignmentDO::getAssignmentStatus, AssignmentStatusEnum.ACCEPTED.getStatus()));
    }

    /**
     * 根据服务单ID批量查询有效分配记录
     */
    default List<DeltaOrderAssignmentDO> selectActiveListByServiceOrderIds(Collection<Long> serviceOrderIds) {
        return selectList(new LambdaQueryWrapper<DeltaOrderAssignmentDO>()
                .in(DeltaOrderAssignmentDO::getServiceOrderId, serviceOrderIds)
                .eq(DeltaOrderAssignmentDO::getAssignmentStatus, AssignmentStatusEnum.ACCEPTED.getStatus()));
    }

    /**
     * 根据服务单ID查询所有分配记录（含历史）
     */
    default List<DeltaOrderAssignmentDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(DeltaOrderAssignmentDO::getServiceOrderId, serviceOrderId);
    }

}
