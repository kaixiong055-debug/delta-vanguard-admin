package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import lombok.Builder;
import lombok.Getter;

/**
 * 俱乐部订单可信跨租户上下文，仅由服务端根据挂牌与派单记录解析。
 */
@Getter
@Builder
public class DeltaClubOrderTenantContext {

    private DeltaOrderMarketListingDO listing;
    private DeltaClubProfileDO club;
    private DeltaServiceOrderDO order;
    private DeltaOrderAssignmentDO assignment;
    private DeltaWorkerDO worker;
    private Long sourceTenantId;
    private Long workerTenantId;
}
