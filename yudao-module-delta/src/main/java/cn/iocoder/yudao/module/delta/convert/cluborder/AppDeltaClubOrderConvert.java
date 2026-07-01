package cn.iocoder.yudao.module.delta.convert.cluborder;

import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.enums.order.DeviceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;

public final class AppDeltaClubOrderConvert {

    public static final AppDeltaClubOrderConvert INSTANCE = new AppDeltaClubOrderConvert();

    private AppDeltaClubOrderConvert() {
    }

    public AppDeltaClubOrderPageRespVO convertPageItem(DeltaOrderMarketListingDO listing,
                                                        DeltaServiceOrderDO order,
                                                        DeltaWorkerDO worker) {
        AppDeltaClubOrderPageRespVO target = new AppDeltaClubOrderPageRespVO();
        fillCommon(target, listing, order);
        target.setAssignedWorkerId(order.getAssignedWorkerId());
        target.setAssignedWorkerDisplayName(worker != null ? worker.getDisplayName() : null);
        target.setClaimTime(listing.getClaimTime());
        target.setAcceptedAt(order.getAcceptedAt());
        target.setCreateTime(order.getCreateTime());
        return target;
    }

    public AppDeltaClubOrderDetailRespVO convertDetail(DeltaOrderMarketListingDO listing,
                                                        DeltaServiceOrderDO order,
                                                        DeltaWorkerDO worker) {
        AppDeltaClubOrderDetailRespVO target = new AppDeltaClubOrderDetailRespVO();
        target.setListingId(listing.getId());
        target.setListingNo(listing.getListingNo());
        target.setServiceOrderId(order.getId());
        target.setServiceOrderNo(order.getServiceOrderNo());
        target.setServiceType(order.getServiceType());
        target.setServiceTypeName(serviceTypeName(order.getServiceType()));
        target.setDeviceType(order.getDeviceType());
        target.setDeviceTypeName(deviceTypeName(order.getDeviceType()));
        target.setServiceAmount(order.getServiceAmount());
        target.setRequirementSummary(listing.getRequirementSummary());
        target.setProductName(order.getProductName());
        target.setSkuName(order.getSkuName());
        target.setProductPicUrl(order.getProductPicUrl());
        target.setCount(order.getCount());
        target.setCustomerRemark(order.getCustomerRemark());
        target.setOrderStatus(order.getStatus());
        target.setOrderStatusName(orderStatusName(order.getStatus()));
        target.setAssignedWorkerId(order.getAssignedWorkerId());
        if (worker != null) {
            target.setAssignedWorkerNo(worker.getWorkerNo());
            target.setAssignedWorkerDisplayName(worker.getDisplayName());
            target.setAssignedWorkerAvatar(worker.getAvatar());
        }
        target.setClaimTime(listing.getClaimTime());
        target.setAcceptedAt(order.getAcceptedAt());
        target.setStartedAt(order.getStartedAt());
        target.setSubmittedAt(order.getSubmittedAt());
        target.setCompletedAt(order.getCompletedAt());
        target.setCreateTime(order.getCreateTime());
        return target;
    }

    public AppDeltaClubOrderWorkerRespVO convertWorker(DeltaWorkerDO worker) {
        AppDeltaClubOrderWorkerRespVO target = new AppDeltaClubOrderWorkerRespVO();
        target.setId(worker.getId());
        target.setWorkerNo(worker.getWorkerNo());
        target.setDisplayName(worker.getDisplayName());
        target.setAvatar(worker.getAvatar());
        target.setLevel(worker.getLevel());
        target.setScore(worker.getScore());
        target.setWorkStatus(worker.getWorkStatus());
        WorkerWorkStatusEnum status = WorkerWorkStatusEnum.valueOf(worker.getWorkStatus());
        target.setWorkStatusName(status != null ? status.getName() : "未知");
        target.setCurrentOrderCount(worker.getCurrentOrderCount());
        target.setMaxOrderCount(worker.getMaxOrderCount());
        return target;
    }

    private void fillCommon(AppDeltaClubOrderPageRespVO target,
                            DeltaOrderMarketListingDO listing, DeltaServiceOrderDO order) {
        target.setListingId(listing.getId());
        target.setListingNo(listing.getListingNo());
        target.setServiceOrderId(order.getId());
        target.setServiceOrderNo(order.getServiceOrderNo());
        target.setServiceType(order.getServiceType());
        target.setServiceTypeName(serviceTypeName(order.getServiceType()));
        target.setDeviceType(order.getDeviceType());
        target.setDeviceTypeName(deviceTypeName(order.getDeviceType()));
        target.setServiceAmount(order.getServiceAmount());
        target.setRequirementSummary(listing.getRequirementSummary());
        target.setOrderStatus(order.getStatus());
        target.setOrderStatusName(orderStatusName(order.getStatus()));
    }

    private String serviceTypeName(Integer value) {
        ServiceTypeEnum type = ServiceTypeEnum.valueOf(value);
        return type != null ? type.getName() : "未知";
    }

    private String deviceTypeName(Integer value) {
        for (DeviceTypeEnum type : DeviceTypeEnum.values()) {
            if (type.getType().equals(value)) {
                return type.getName();
            }
        }
        return "未知";
    }

    private String orderStatusName(Integer value) {
        for (ServiceOrderStatusEnum status : ServiceOrderStatusEnum.values()) {
            if (status.getStatus().equals(value)) {
                return status.getName();
            }
        }
        return "未知";
    }
}
