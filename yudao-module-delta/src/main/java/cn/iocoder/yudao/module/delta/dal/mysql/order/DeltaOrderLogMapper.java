package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单日志 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderLogMapper extends BaseMapperX<DeltaOrderLogDO> {

    /**
     * 根据服务单ID查询操作日志列表
     */
    default List<DeltaOrderLogDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(DeltaOrderLogDO::getServiceOrderId, serviceOrderId);
    }

}
