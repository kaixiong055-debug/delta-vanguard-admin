package cn.iocoder.yudao.module.delta.convert.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * App 订单市场安全字段转换器。
 */
public final class AppDeltaOrderMarketConvert {

    public static final AppDeltaOrderMarketConvert INSTANCE = new AppDeltaOrderMarketConvert();

    private AppDeltaOrderMarketConvert() {
    }

    public AppDeltaOrderMarketRespVO convert(DeltaOrderMarketListingDO source) {
        if (source == null) {
            return null;
        }
        AppDeltaOrderMarketRespVO target = new AppDeltaOrderMarketRespVO();
        target.setId(source.getId());
        target.setListingNo(source.getListingNo());
        target.setServiceType(source.getServiceType());
        ServiceTypeEnum serviceType = ServiceTypeEnum.valueOf(source.getServiceType());
        target.setServiceTypeName(serviceType != null ? serviceType.getName() : "未知");
        target.setServiceAmount(source.getServiceAmount());
        target.setRequirementSummary(source.getRequirementSummary());
        target.setListingStatus(source.getListingStatus());
        DeltaOrderMarketStatusEnum status =
                DeltaOrderMarketStatusEnum.fromStatus(source.getListingStatus());
        target.setListingStatusName(status != null ? status.getName() : "未知");
        target.setPublishTime(source.getPublishTime());
        target.setExpireTime(source.getExpireTime());
        target.setClaimTime(source.getClaimTime());
        return target;
    }

    public List<AppDeltaOrderMarketRespVO> convertList(List<DeltaOrderMarketListingDO> list) {
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    public PageResult<AppDeltaOrderMarketRespVO> convertPage(
            PageResult<DeltaOrderMarketListingDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }
}
