package cn.iocoder.yudao.module.delta.dal.mysql.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 平台订单市场挂牌 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderMarketListingMapper extends BaseMapperX<DeltaOrderMarketListingDO> {

    /**
     * 根据挂牌编号查询
     */
    default DeltaOrderMarketListingDO selectByListingNo(String listingNo) {
        return selectOne(new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getListingNo, listingNo));
    }

    /**
     * 根据服务订单ID查询（同一服务订单只有一个有效挂牌，但这里返回可能多条以兼容）
     */
    default DeltaOrderMarketListingDO selectByServiceOrderId(Long serviceOrderId) {
        return selectOne(new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getServiceOrderId, serviceOrderId)
                .orderByDesc(DeltaOrderMarketListingDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 分页查询挂牌（平台管理员）
     */
    default PageResult<DeltaOrderMarketListingDO> selectPage(Integer pageNo, Integer pageSize,
                                                              String listingNo, String serviceOrderNo,
                                                              Integer serviceType, Integer listingStatus,
                                                              Long claimedClubId, Long claimedClubTenantId,
                                                              LocalDateTime[] publishTime, LocalDateTime[] createTime) {
        return selectPage(new cn.iocoder.yudao.framework.common.pojo.PageParam() {
            @Override
            public Integer getPageNo() { return pageNo; }
            @Override
            public Integer getPageSize() { return pageSize; }
        }, new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eqIfPresent(DeltaOrderMarketListingDO::getListingNo, listingNo)
                .eqIfPresent(DeltaOrderMarketListingDO::getServiceOrderNo, serviceOrderNo)
                .eqIfPresent(DeltaOrderMarketListingDO::getServiceType, serviceType)
                .eqIfPresent(DeltaOrderMarketListingDO::getListingStatus, listingStatus)
                .eqIfPresent(DeltaOrderMarketListingDO::getClaimedClubId, claimedClubId)
                .eqIfPresent(DeltaOrderMarketListingDO::getClaimedClubTenantId, claimedClubTenantId)
                .betweenIfPresent(DeltaOrderMarketListingDO::getPublishTime, publishTime)
                .betweenIfPresent(DeltaOrderMarketListingDO::getCreateTime, createTime)
                .orderByDesc(DeltaOrderMarketListingDO::getId));
    }

    /**
     * CAS 更新状态为 CLAIMED
     */
    default int updateClaimCas(Long id, Integer version, Long claimedClubId, Long claimedClubTenantId,
                                LocalDateTime claimTime) {
        return update(null, new LambdaUpdateWrapper<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getId, id)
                .eq(DeltaOrderMarketListingDO::getVersion, version)
                .eq(DeltaOrderMarketListingDO::getListingStatus, 0)
                .isNull(DeltaOrderMarketListingDO::getClaimedClubId)
                .set(DeltaOrderMarketListingDO::getListingStatus, 1)
                .set(DeltaOrderMarketListingDO::getClaimedClubId, claimedClubId)
                .set(DeltaOrderMarketListingDO::getClaimedClubTenantId, claimedClubTenantId)
                .set(DeltaOrderMarketListingDO::getClaimTime, claimTime)
                .set(DeltaOrderMarketListingDO::getActiveFlag, 0)
                .set(DeltaOrderMarketListingDO::getVersion, version + 1));
    }

    /**
     * CAS 更新状态（用于撤回/过期/关闭，并设置 activeFlag=0 释放唯一约束）
     */
    default int updateStatusCas(Long id, Integer version, Integer currentStatus, Integer newStatus) {
        return update(null, new LambdaUpdateWrapper<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getId, id)
                .eq(DeltaOrderMarketListingDO::getVersion, version)
                .eq(DeltaOrderMarketListingDO::getListingStatus, currentStatus)
                .set(DeltaOrderMarketListingDO::getListingStatus, newStatus)
                .set(DeltaOrderMarketListingDO::getActiveFlag, 0)
                .set(DeltaOrderMarketListingDO::getVersion, version + 1));
    }

    /**
     * 查询可抢的挂牌（AVAILABLE且未过期）
     */
    default List<DeltaOrderMarketListingDO> selectAvailable(LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getListingStatus, 0)
                .and(w -> w.isNull(DeltaOrderMarketListingDO::getExpireTime)
                        .or().gt(DeltaOrderMarketListingDO::getExpireTime, now))
                .orderByAsc(DeltaOrderMarketListingDO::getId));
    }

    /**
     * 分页查询可抢的挂牌（在数据库层面按服务类型过滤，避免Java端N+1过滤）
     */
    default PageResult<DeltaOrderMarketListingDO> selectAvailablePageByServiceTypes(
            Integer pageNo, Integer pageSize, LocalDateTime now, Set<Integer> serviceTypes) {
        return selectPage(new cn.iocoder.yudao.framework.common.pojo.PageParam() {
            @Override
            public Integer getPageNo() { return pageNo; }
            @Override
            public Integer getPageSize() { return pageSize; }
        }, new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getListingStatus, 0)
                .and(w -> w.isNull(DeltaOrderMarketListingDO::getExpireTime)
                        .or().gt(DeltaOrderMarketListingDO::getExpireTime, now))
                .in(DeltaOrderMarketListingDO::getServiceType, serviceTypes)
                .orderByAsc(DeltaOrderMarketListingDO::getId));
    }

    /**
     * 查询已过期的 AVAILABLE 挂牌
     */
    default List<DeltaOrderMarketListingDO> selectExpiredAvailable(LocalDateTime now, int limit) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getListingStatus, 0)
                .le(DeltaOrderMarketListingDO::getExpireTime, now)
                .orderByAsc(DeltaOrderMarketListingDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 查询已 CLAIMED 的挂牌（按俱乐部租户ID过滤、分页）
     */
    default PageResult<DeltaOrderMarketListingDO> selectClaimedPage(Integer pageNo, Integer pageSize,
                                                                     Long claimedClubId,
                                                                     Long claimedClubTenantId) {
        return selectPage(new cn.iocoder.yudao.framework.common.pojo.PageParam() {
            @Override
            public Integer getPageNo() { return pageNo; }
            @Override
            public Integer getPageSize() { return pageSize; }
        }, new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getClaimedClubId, claimedClubId)
                .eq(DeltaOrderMarketListingDO::getClaimedClubTenantId, claimedClubTenantId)
                .eq(DeltaOrderMarketListingDO::getListingStatus, 1)
                .orderByDesc(DeltaOrderMarketListingDO::getId));
    }

    /**
     * 查询某个俱乐部 CLAIMED 的挂牌列表（含 serviceOrderId + sourceTenantId，用于按服务订单终态过滤）
     */
    default List<DeltaOrderMarketListingDO> selectClaimedByClub(Long claimedClubId,
                                                                 Long claimedClubTenantId) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderMarketListingDO>()
                .eq(DeltaOrderMarketListingDO::getClaimedClubId, claimedClubId)
                .eq(DeltaOrderMarketListingDO::getClaimedClubTenantId, claimedClubTenantId)
                .eq(DeltaOrderMarketListingDO::getListingStatus, 1)
                .select(DeltaOrderMarketListingDO::getId, DeltaOrderMarketListingDO::getServiceOrderId,
                        DeltaOrderMarketListingDO::getSourceTenantId));
    }
}
