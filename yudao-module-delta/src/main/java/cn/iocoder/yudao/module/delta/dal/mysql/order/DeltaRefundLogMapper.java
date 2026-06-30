package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 退款日志 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaRefundLogMapper extends BaseMapperX<DeltaRefundLogDO> {

    default List<DeltaRefundLogDO> selectListByRefundRecordId(Long refundRecordId) {
        return selectList(DeltaRefundLogDO::getRefundRecordId, refundRecordId);
    }

}
