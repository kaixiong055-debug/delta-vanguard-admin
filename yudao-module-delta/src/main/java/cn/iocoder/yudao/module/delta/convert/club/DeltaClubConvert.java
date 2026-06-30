package cn.iocoder.yudao.module.delta.convert.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.club.vo.DeltaClubRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 俱乐部 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaClubConvert {

    DeltaClubConvert INSTANCE = Mappers.getMapper(DeltaClubConvert.class);

    DeltaClubRespVO convert(DeltaClubProfileDO bean);
    List<DeltaClubRespVO> convertList(List<DeltaClubProfileDO> list);
    PageResult<DeltaClubRespVO> convertPage(PageResult<DeltaClubProfileDO> page);

    default void fillBusinessStatusName(List<DeltaClubRespVO> list) {
        for (DeltaClubRespVO vo : list) {
            if (vo.getBusinessStatus() != null) {
                vo.setBusinessStatusName(
                        DeltaClubBusinessStatusEnum.isEnabled(vo.getBusinessStatus()) ? "启用" : "停用");
            }
        }
    }

}
