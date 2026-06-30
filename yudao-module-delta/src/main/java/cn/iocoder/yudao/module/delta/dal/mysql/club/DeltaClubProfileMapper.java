package cn.iocoder.yudao.module.delta.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.club.vo.DeltaClubPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 俱乐部档案 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaClubProfileMapper extends BaseMapperX<DeltaClubProfileDO> {

    /** 根据 tenantId 查询 */
    default DeltaClubProfileDO selectByTenantId(Long tenantId) {
        return selectOne(new LambdaQueryWrapperX<DeltaClubProfileDO>()
                .eq(DeltaClubProfileDO::getTenantId, tenantId));
    }

    /** 根据 clubCode 查询 */
    default DeltaClubProfileDO selectByClubCode(String clubCode) {
        return selectOne(new LambdaQueryWrapperX<DeltaClubProfileDO>()
                .eq(DeltaClubProfileDO::getClubCode, clubCode));
    }

    /** 根据 ownerMemberId 查询 */
    default DeltaClubProfileDO selectByOwnerMemberId(Long ownerMemberId) {
        return selectOne(new LambdaQueryWrapperX<DeltaClubProfileDO>()
                .eq(DeltaClubProfileDO::getOwnerMemberId, ownerMemberId));
    }

    /** 分页查询（平台跨租户） */
    default PageResult<DeltaClubProfileDO> selectPage(DeltaClubPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaClubProfileDO>()
                .eqIfPresent(DeltaClubProfileDO::getClubCode, reqVO.getClubCode())
                .likeIfPresent(DeltaClubProfileDO::getClubName, reqVO.getClubName())
                .eqIfPresent(DeltaClubProfileDO::getOwnerMemberId, reqVO.getOwnerMemberId())
                .eqIfPresent(DeltaClubProfileDO::getBusinessStatus, reqVO.getBusinessStatus())
                .betweenIfPresent(DeltaClubProfileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaClubProfileDO::getId));
    }

}
