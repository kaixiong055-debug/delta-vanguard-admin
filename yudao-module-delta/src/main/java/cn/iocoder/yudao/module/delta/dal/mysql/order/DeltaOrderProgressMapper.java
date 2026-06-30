package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 服务进度 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderProgressMapper extends BaseMapperX<DeltaOrderProgressDO> {

    /**
     * 根据服务单ID查询进度列表（按创建时间正序）
     */
    default List<DeltaOrderProgressDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(new LambdaQueryWrapper<DeltaOrderProgressDO>()
                .eq(DeltaOrderProgressDO::getServiceOrderId, serviceOrderId)
                .orderByAsc(DeltaOrderProgressDO::getCreateTime));
    }

    /**
     * 根据服务单ID列表批量查询进度
     */
    default List<DeltaOrderProgressDO> selectListByServiceOrderIds(List<Long> serviceOrderIds) {
        return selectList(new LambdaQueryWrapper<DeltaOrderProgressDO>()
                .in(DeltaOrderProgressDO::getServiceOrderId, serviceOrderIds)
                .orderByAsc(DeltaOrderProgressDO::getCreateTime));
    }

    /**
     * 根据服务单ID和打手ID查询进度列表
     */
    default List<DeltaOrderProgressDO> selectListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId) {
        return selectList(new LambdaQueryWrapper<DeltaOrderProgressDO>()
                .eq(DeltaOrderProgressDO::getServiceOrderId, serviceOrderId)
                .eq(DeltaOrderProgressDO::getWorkerId, workerId)
                .orderByAsc(DeltaOrderProgressDO::getCreateTime));
    }

}
