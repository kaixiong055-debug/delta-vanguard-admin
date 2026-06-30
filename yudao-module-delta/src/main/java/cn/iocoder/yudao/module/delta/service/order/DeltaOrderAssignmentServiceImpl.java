package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAssignmentMapper;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ASSIGNMENT_NOT_FOUND;

/**
 * 派单记录 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaOrderAssignmentServiceImpl implements DeltaOrderAssignmentService {

    @Resource
    private DeltaOrderAssignmentMapper deltaOrderAssignmentMapper;

    @Override
    public DeltaOrderAssignmentDO getActiveAssignmentByServiceOrderId(Long serviceOrderId) {
        return deltaOrderAssignmentMapper.selectActiveByServiceOrderId(serviceOrderId);
    }

    @Override
    public List<DeltaOrderAssignmentDO> getActiveAssignmentsByWorkerId(Long workerId) {
        return deltaOrderAssignmentMapper.selectActiveListByWorkerId(workerId);
    }

    @Override
    public List<DeltaOrderAssignmentDO> getAssignmentHistory(Long serviceOrderId) {
        return deltaOrderAssignmentMapper.selectListByServiceOrderId(serviceOrderId);
    }

    @Override
    public DeltaOrderAssignmentDO createAssignment(DeltaOrderAssignmentDO assignment) {
        deltaOrderAssignmentMapper.insert(assignment);
        return assignment;
    }

    @Override
    public void cancelAssignment(Long assignmentId, String reason) {
        DeltaOrderAssignmentDO assignment = deltaOrderAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw exception(ASSIGNMENT_NOT_FOUND);
        }
        assignment.setAssignmentStatus(AssignmentStatusEnum.CANCELED.getStatus());
        assignment.setReason(reason);
        deltaOrderAssignmentMapper.updateById(assignment);
    }

    @Override
    public DeltaOrderAssignmentDO getAssignment(Long id) {
        return deltaOrderAssignmentMapper.selectById(id);
    }

}
