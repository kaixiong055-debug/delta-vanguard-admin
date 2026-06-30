package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderReworkDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 返工记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderReworkMapper extends BaseMapperX<DeltaOrderReworkDO> {

    /**
     * 根据服务单ID查询返工记录列表
     */
    default List<DeltaOrderReworkDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderReworkDO>()
                .eq(DeltaOrderReworkDO::getServiceOrderId, serviceOrderId)
                .orderByDesc(DeltaOrderReworkDO::getId));
    }

    /**
     * 根据服务单ID批量查询返工记录
     */
    default List<DeltaOrderReworkDO> selectListByServiceOrderIds(List<Long> serviceOrderIds) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderReworkDO>()
                .in(DeltaOrderReworkDO::getServiceOrderId, serviceOrderIds)
                .orderByDesc(DeltaOrderReworkDO::getId));
    }

    /**
     * 统计服务单返工次数
     */
    default long selectCountByServiceOrderId(Long serviceOrderId) {
        return selectCount(new LambdaQueryWrapperX<DeltaOrderReworkDO>()
                .eq(DeltaOrderReworkDO::getServiceOrderId, serviceOrderId));
    }

}
