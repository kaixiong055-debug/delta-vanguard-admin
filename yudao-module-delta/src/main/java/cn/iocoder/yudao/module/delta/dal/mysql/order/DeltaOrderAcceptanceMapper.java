package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAcceptanceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 验收记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderAcceptanceMapper extends BaseMapperX<DeltaOrderAcceptanceDO> {

    /**
     * 根据服务单ID查询验收记录列表
     */
    default List<DeltaOrderAcceptanceDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderAcceptanceDO>()
                .eq(DeltaOrderAcceptanceDO::getServiceOrderId, serviceOrderId)
                .orderByDesc(DeltaOrderAcceptanceDO::getId));
    }

    /**
     * 根据服务单ID批量查询验收记录
     */
    default List<DeltaOrderAcceptanceDO> selectListByServiceOrderIds(List<Long> serviceOrderIds) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderAcceptanceDO>()
                .in(DeltaOrderAcceptanceDO::getServiceOrderId, serviceOrderIds)
                .orderByDesc(DeltaOrderAcceptanceDO::getId));
    }

}
