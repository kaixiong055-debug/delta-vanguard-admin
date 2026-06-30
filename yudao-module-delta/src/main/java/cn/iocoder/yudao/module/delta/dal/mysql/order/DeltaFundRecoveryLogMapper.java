package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaFundRecoveryLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 追回日志 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaFundRecoveryLogMapper extends BaseMapperX<DeltaFundRecoveryLogDO> {

    default List<DeltaFundRecoveryLogDO> selectListByRecoveryId(Long recoveryId) {
        return selectList(DeltaFundRecoveryLogDO::getRecoveryId, recoveryId);
    }

}
