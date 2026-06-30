package cn.iocoder.yudao.module.delta.dal.mysql.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 商品服务配置 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaProductServiceConfigMapper extends BaseMapperX<DeltaProductServiceConfigDO> {

    default DeltaProductServiceConfigDO selectBySkuId(Long skuId) {
        return selectOne(DeltaProductServiceConfigDO::getSkuId, skuId);
    }

    default DeltaProductServiceConfigDO selectBySpuId(Long spuId) {
        return selectOne(DeltaProductServiceConfigDO::getSpuId, spuId);
    }

    default List<DeltaProductServiceConfigDO> selectListBySkuIds(Collection<Long> skuIds) {
        return selectList(DeltaProductServiceConfigDO::getSkuId, skuIds);
    }

    default List<DeltaProductServiceConfigDO> selectListBySpuIds(Collection<Long> spuIds) {
        return selectList(DeltaProductServiceConfigDO::getSpuId, spuIds);
    }

    default PageResult<DeltaProductServiceConfigDO> selectPage(DeltaProductServiceConfigPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO));
    }

    default LambdaQueryWrapper<DeltaProductServiceConfigDO> buildQueryWrapper(DeltaProductServiceConfigPageReqVO reqVO) {
        return new LambdaQueryWrapperX<DeltaProductServiceConfigDO>()
                .eqIfPresent(DeltaProductServiceConfigDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(DeltaProductServiceConfigDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(DeltaProductServiceConfigDO::getServiceType, reqVO.getServiceType())
                .eqIfPresent(DeltaProductServiceConfigDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(DeltaProductServiceConfigDO::getEnabled, reqVO.getEnabled())
                .orderByDesc(DeltaProductServiceConfigDO::getId);
    }

}
