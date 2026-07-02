package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import lombok.Builder;
import lombok.Getter;

/**
 * 打手履约访问上下文。租户与身份字段均由服务端解析，不接受客户端输入。
 */
@Getter
@Builder
public class DeltaWorkerOrderAccessContext {

    private DeltaWorkerDO worker;
    private Long workerTenantId;
    private DeltaClubProfileDO club;
    private DeltaOrderMarketListingDO listing;
    private DeltaOrderAssignmentDO assignment;
    private DeltaServiceOrderDO order;
    private Long sourceTenantId;
    private boolean clubOrder;
}
