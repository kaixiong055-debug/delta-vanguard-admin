package cn.iocoder.yudao.module.delta.convert.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.DeltaProductServiceConfigRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品服务配置 Convert
 */
@Mapper
public interface DeltaProductServiceConfigConvert {

    DeltaProductServiceConfigConvert INSTANCE = Mappers.getMapper(DeltaProductServiceConfigConvert.class);

    DeltaProductServiceConfigRespVO convert(DeltaProductServiceConfigDO bean);

    List<DeltaProductServiceConfigRespVO> convertList(List<DeltaProductServiceConfigDO> list);

    PageResult<DeltaProductServiceConfigRespVO> convertPage(PageResult<DeltaProductServiceConfigDO> page);

}
