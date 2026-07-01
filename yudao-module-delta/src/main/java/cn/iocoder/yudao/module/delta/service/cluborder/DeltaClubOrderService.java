package cn.iocoder.yudao.module.delta.service.cluborder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAssignWorkerReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAvailableWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;

public interface DeltaClubOrderService {

    PageResult<AppDeltaClubOrderPageRespVO> getPageForMember(
            Long memberUserId, AppDeltaClubOrderPageReqVO reqVO);

    AppDeltaClubOrderDetailRespVO getDetailForMember(Long memberUserId, Long listingId);

    PageResult<AppDeltaClubOrderWorkerRespVO> getAvailableWorkerPageForMember(
            Long memberUserId, AppDeltaClubOrderAvailableWorkerPageReqVO reqVO);

    void assignWorkerForMember(Long memberUserId, AppDeltaClubOrderAssignWorkerReqVO reqVO);
}
