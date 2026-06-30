package cn.iocoder.yudao.module.delta.convert.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.DeltaServiceOrderRespVO;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.vo.AppDeltaOrderPoolRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.enums.order.DeviceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 服务履约订单 Convert
 */
@Mapper
public interface DeltaServiceOrderConvert {

    DeltaServiceOrderConvert INSTANCE = Mappers.getMapper(DeltaServiceOrderConvert.class);

    // App 客户列表
    AppDeltaServiceOrderRespVO convert(DeltaServiceOrderDO bean);
    List<AppDeltaServiceOrderRespVO> convertList(List<DeltaServiceOrderDO> list);
    PageResult<AppDeltaServiceOrderRespVO> convertPage(PageResult<DeltaServiceOrderDO> page);

    // App 客户详情
    AppDeltaServiceOrderDetailRespVO convertDetail(DeltaServiceOrderDO bean);

    // Admin 列表
    DeltaServiceOrderRespVO convert2(DeltaServiceOrderDO bean);
    List<DeltaServiceOrderRespVO> convertList2(List<DeltaServiceOrderDO> list);
    PageResult<DeltaServiceOrderRespVO> convertPage2(PageResult<DeltaServiceOrderDO> page);

    // Admin 带会员信息
    default DeltaServiceOrderRespVO convert2(DeltaServiceOrderDO bean, MemberUserRespDTO memberUser) {
        DeltaServiceOrderRespVO vo = convert2(bean);
        if (memberUser != null) {
            vo.setMemberNickname(memberUser.getNickname());
            vo.setMemberAvatar(memberUser.getAvatar());
        }
        return vo;
    }

    default PageResult<DeltaServiceOrderRespVO> convertPage2(PageResult<DeltaServiceOrderDO> pageResult,
                                                              java.util.Map<Long, MemberUserRespDTO> memberUserMap) {
        PageResult<DeltaServiceOrderRespVO> result = convertPage2(pageResult);
        result.getList().forEach(vo -> {
            MemberUserRespDTO memberUser = memberUserMap.get(vo.getBuyerUserId());
            if (memberUser != null) {
                vo.setMemberNickname(memberUser.getNickname());
                vo.setMemberAvatar(memberUser.getAvatar());
            }
        });
        return result;
    }

    // Phase 4 订单池/打手订单
    @Named("convertPool")
    AppDeltaOrderPoolRespVO convertPool(DeltaServiceOrderDO bean);

    @IterableMapping(qualifiedByName = "convertPool")
    List<AppDeltaOrderPoolRespVO> convertPoolList(List<DeltaServiceOrderDO> list);

    default PageResult<AppDeltaOrderPoolRespVO> convertPoolPage(PageResult<DeltaServiceOrderDO> page) {
        PageResult<AppDeltaOrderPoolRespVO> result = new PageResult<>();
        result.setList(convertPoolList(page.getList()));
        result.setTotal(page.getTotal());
        result.getList().forEach(this::fillPoolNames);
        return result;
    }

    @Named("convertPoolDetail")
    default AppDeltaOrderPoolRespVO convertPoolDetail(DeltaServiceOrderDO bean) {
        AppDeltaOrderPoolRespVO vo = convertPool(bean);
        fillPoolNames(vo);
        return vo;
    }

    // 打手订单详情
    AppDeltaWorkerOrderDetailRespVO convertWorkerOrderDetail(DeltaServiceOrderDO bean);

    default void fillPoolNames(AppDeltaOrderPoolRespVO vo) {
        if (vo.getServiceType() != null) {
            for (ServiceTypeEnum e : ServiceTypeEnum.values()) {
                if (e.getType().equals(vo.getServiceType())) {
                    vo.setServiceTypeName(e.getName());
                    break;
                }
            }
        }
        if (vo.getDeviceType() != null) {
            for (DeviceTypeEnum e : DeviceTypeEnum.values()) {
                if (e.getType().equals(vo.getDeviceType())) {
                    vo.setDeviceTypeName(e.getName());
                    break;
                }
            }
        }
        if (vo.getStatus() != null) {
            for (ServiceOrderStatusEnum e : ServiceOrderStatusEnum.values()) {
                if (e.getStatus().equals(vo.getStatus())) {
                    vo.setStatusName(e.getName());
                    break;
                }
            }
        }
    }

}
