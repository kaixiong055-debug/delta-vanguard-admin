package cn.iocoder.yudao.module.delta.dal.mysql.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 市场操作日志 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderMarketLogMapper extends BaseMapperX<DeltaOrderMarketLogDO> {

    /**
     * 按挂牌ID查询日志列表
     */
    default java.util.List<DeltaOrderMarketLogDO> selectByListingId(Long listingId) {
        return selectList(new LambdaQueryWrapperX<DeltaOrderMarketLogDO>()
                .eq(DeltaOrderMarketLogDO::getListingId, listingId)
                .orderByAsc(DeltaOrderMarketLogDO::getId));
    }

    /**
     * 分页查询日志
     */
    default PageResult<DeltaOrderMarketLogDO> selectPage(Long listingId, Long serviceOrderId, String operationType) {
        return selectPage(new cn.iocoder.yudao.framework.common.pojo.PageParam() {
            @Override
            public Integer getPageNo() { return 1; }
            @Override
            public Integer getPageSize() { return 100; }
        }, new LambdaQueryWrapperX<DeltaOrderMarketLogDO>()
                .eqIfPresent(DeltaOrderMarketLogDO::getListingId, listingId)
                .eqIfPresent(DeltaOrderMarketLogDO::getServiceOrderId, serviceOrderId)
                .eqIfPresent(DeltaOrderMarketLogDO::getOperationType, operationType)
                .orderByDesc(DeltaOrderMarketLogDO::getId));
    }
}
