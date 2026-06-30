package cn.iocoder.yudao.module.delta.convert.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.DeltaOrderMarketListingRespVO;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.DeltaOrderMarketLogRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 订单市场 Convert
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderMarketConvert {

    DeltaOrderMarketConvert INSTANCE = Mappers.getMapper(DeltaOrderMarketConvert.class);

    DeltaOrderMarketListingRespVO convert(DeltaOrderMarketListingDO bean);

    List<DeltaOrderMarketListingRespVO> convertList(List<DeltaOrderMarketListingDO> list);

    PageResult<DeltaOrderMarketListingRespVO> convertPage(PageResult<DeltaOrderMarketListingDO> page);

    DeltaOrderMarketLogRespVO convertLog(DeltaOrderMarketLogDO bean);

    List<DeltaOrderMarketLogRespVO> convertLogList(List<DeltaOrderMarketLogDO> list);

    /**
     * 填充挂牌状态名称和服务类型名称
     */
    default void fillStatusAndServiceTypeNames(List<DeltaOrderMarketListingRespVO> list) {
        if (list == null) return;
        for (DeltaOrderMarketListingRespVO vo : list) {
            DeltaOrderMarketStatusEnum statusEnum = DeltaOrderMarketStatusEnum.fromStatus(vo.getListingStatus());
            vo.setListingStatusName(statusEnum != null ? statusEnum.getName() : "未知");
            ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.valueOf(vo.getServiceType());
            vo.setServiceTypeName(serviceTypeEnum != null ? serviceTypeEnum.getName() : "未知");
        }
    }

    /**
     * 填充日志操作类型名称
     */
    default void fillLogOperationTypeNames(List<DeltaOrderMarketLogRespVO> list) {
        if (list == null) return;
        for (DeltaOrderMarketLogRespVO vo : list) {
            DeltaOrderMarketOperationTypeEnum typeEnum = DeltaOrderMarketOperationTypeEnum.fromType(vo.getOperationType());
            vo.setOperationTypeName(typeEnum != null ? typeEnum.getDescription() : "未知");
        }
    }
}
