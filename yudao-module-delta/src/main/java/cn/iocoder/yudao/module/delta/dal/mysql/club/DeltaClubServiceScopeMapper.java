package cn.iocoder.yudao.module.delta.dal.mysql.club;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 俱乐部服务范围 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaClubServiceScopeMapper extends BaseMapperX<DeltaClubServiceScopeDO> {

    /** 根据 clubProfileId 查询服务范围列表 */
    default List<DeltaClubServiceScopeDO> selectListByClubProfileId(Long clubProfileId) {
        return selectList(new LambdaQueryWrapperX<DeltaClubServiceScopeDO>()
                .eq(DeltaClubServiceScopeDO::getClubProfileId, clubProfileId));
    }

    /** 根据 tenantId 查询服务范围列表 */
    default List<DeltaClubServiceScopeDO> selectListByTenantId(Long tenantId) {
        return selectList(new LambdaQueryWrapperX<DeltaClubServiceScopeDO>()
                .eq(DeltaClubServiceScopeDO::getTenantId, tenantId));
    }

    /** 根据 clubProfileId 删除所有服务范围 */
    default void deleteByClubProfileId(Long clubProfileId) {
        delete(new LambdaQueryWrapperX<DeltaClubServiceScopeDO>()
                .eq(DeltaClubServiceScopeDO::getClubProfileId, clubProfileId));
    }

}
