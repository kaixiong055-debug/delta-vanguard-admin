package cn.iocoder.yudao.module.delta.controller.app.cluborder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAssignWorkerReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAvailableWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;
import cn.iocoder.yudao.module.delta.service.cluborder.DeltaClubOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 俱乐部履约订单")
@RestController
@RequestMapping("/delta/club-order")
@Validated
public class AppDeltaClubOrderController {

    @Resource
    private DeltaClubOrderService clubOrderService;

    @GetMapping("/page")
    @Operation(summary = "分页查询当前俱乐部已接挂牌履约信息")
    public CommonResult<PageResult<AppDeltaClubOrderPageRespVO>> getPage(
            @Valid AppDeltaClubOrderPageReqVO reqVO) {
        return success(clubOrderService.getPageForMember(getLoginUserId(), reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "查询当前俱乐部已接挂牌履约详情")
    public CommonResult<AppDeltaClubOrderDetailRespVO> getDetail(
            @RequestParam("listingId") Long listingId) {
        return success(clubOrderService.getDetailForMember(getLoginUserId(), listingId));
    }

    @GetMapping("/available-worker-page")
    @Operation(summary = "分页查询当前俱乐部可分派打手")
    public CommonResult<PageResult<AppDeltaClubOrderWorkerRespVO>> getAvailableWorkerPage(
            @Valid AppDeltaClubOrderAvailableWorkerPageReqVO reqVO) {
        return success(clubOrderService.getAvailableWorkerPageForMember(getLoginUserId(), reqVO));
    }

    @PostMapping("/assign-worker")
    @Operation(summary = "为已接挂牌分派本俱乐部打手")
    public CommonResult<Boolean> assignWorker(
            @RequestBody @Valid AppDeltaClubOrderAssignWorkerReqVO reqVO) {
        clubOrderService.assignWorkerForMember(getLoginUserId(), reqVO);
        return success(true);
    }
}
