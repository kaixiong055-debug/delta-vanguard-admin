package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleArbitrationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 仲裁记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaAfterSaleArbitrationMapper extends BaseMapperX<DeltaAfterSaleArbitrationDO> {

    default List<DeltaAfterSaleArbitrationDO> selectListByAfterSaleId(Long afterSaleId) {
        return selectList(new LambdaQueryWrapperX<DeltaAfterSaleArbitrationDO>()
                .eq(DeltaAfterSaleArbitrationDO::getAfterSaleId, afterSaleId)
                .orderByDesc(DeltaAfterSaleArbitrationDO::getId));
    }

    default DeltaAfterSaleArbitrationDO selectByAfterSaleId(Long afterSaleId) {
        return selectOne(new LambdaQueryWrapperX<DeltaAfterSaleArbitrationDO>()
                .eq(DeltaAfterSaleArbitrationDO::getAfterSaleId, afterSaleId)
                .last("LIMIT 1"));
    }

}
