package cn.iocoder.yudao.module.delta.controller.admin.serviceorder;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.*;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderTimelineRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderAcceptanceRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderReworkRespVO;
import cn.iocoder.yudao.module.delta.convert.order.DeltaServiceOrderConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.service.order.DeltaServiceOrderService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Admin - 服务履约订单 Controller
 */
@Tag(name = "管理后台 - 服务履约订单")
@RestController
@RequestMapping("/delta/service-order")
@Validated
public class DeltaServiceOrderController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;
    @Resource
    private MemberUserApi memberUserApi;

    // ====== Phase 3 已有 ======

    @GetMapping("/page")
    @Operation(summary = "分页查询服务履约订单")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<PageResult<DeltaServiceOrderRespVO>> getServiceOrderPage(
            @Valid DeltaServiceOrderPageReqVO pageReqVO) {
        PageResult<DeltaServiceOrderDO> pageResult = deltaServiceOrderService.getServiceOrderPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        Set<Long> userIds = pageResult.getList().stream()
                .map(DeltaServiceOrderDO::getBuyerUserId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberUserMap = memberUserApi.getUserMap(userIds);
        return success(DeltaServiceOrderConvert.INSTANCE.convertPage2(pageResult, memberUserMap));
    }

    @GetMapping("/get")
    @Operation(summary = "查询服务履约订单详情")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<DeltaServiceOrderRespVO> getServiceOrderDetail(@RequestParam("id") Long id) {
        DeltaServiceOrderDO order = deltaServiceOrderService.getServiceOrder(id);
        if (order == null) {
            return success(null);
        }
        MemberUserRespDTO memberUser = memberUserApi.getUser(order.getBuyerUserId());
        return success(DeltaServiceOrderConvert.INSTANCE.convert2(order, memberUser));
    }

    // ====== Phase 4 后台操作 ======

    @PostMapping("/confirm")
    @Operation(summary = "确认服务单（进入订单池）")
    @PreAuthorize("@ss.hasPermission('delta:service-order:confirm')")
    public CommonResult<Boolean> confirmServiceOrder(
            @RequestBody @Valid DeltaServiceOrderConfirmReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.confirmServiceOrder(reqVO.getId(), reqVO.getRemark(), adminUserId);
        return success(true);
    }

    @PostMapping("/dispatch")
    @Operation(summary = "人工派单")
    @PreAuthorize("@ss.hasPermission('delta:service-order:dispatch')")
    public CommonResult<Boolean> dispatchOrder(
            @RequestBody @Valid DeltaServiceOrderDispatchReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.dispatchOrder(reqVO.getServiceOrderId(), reqVO.getWorkerId(),
                reqVO.getRemark(), adminUserId);
        return success(true);
    }

    @PostMapping("/reassign")
    @Operation(summary = "改派打手")
    @PreAuthorize("@ss.hasPermission('delta:service-order:reassign')")
    public CommonResult<Boolean> reassignOrder(
            @RequestBody @Valid DeltaServiceOrderReassignReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.reassignOrder(reqVO.getServiceOrderId(), reqVO.getNewWorkerId(),
                reqVO.getReason(), adminUserId);
        return success(true);
    }

    @PostMapping("/return-pool")
    @Operation(summary = "退回订单池")
    @PreAuthorize("@ss.hasPermission('delta:service-order:return-pool')")
    public CommonResult<Boolean> returnOrderToPool(
            @RequestBody @Valid DeltaServiceOrderReturnPoolReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.returnOrderToPool(reqVO.getServiceOrderId(),
                reqVO.getReason(), adminUserId);
        return success(true);
    }

    // ====== Phase 5 后台查看进度/凭证 ======

    @GetMapping("/progress-list")
    @Operation(summary = "后台查询服务单进度")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<List<DeltaOrderProgressDO>> getProgressList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderProgressDO> list = deltaServiceOrderService.getProgressListByServiceOrderId(serviceOrderId);
        return success(list);
    }

    @GetMapping("/evidence-list")
    @Operation(summary = "后台查询服务单凭证")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<List<DeltaOrderEvidenceDO>> getEvidenceList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderEvidenceDO> list = deltaServiceOrderService.getEvidenceListByServiceOrderId(serviceOrderId);
        return success(list);
    }

    @GetMapping("/timeline")
    @Operation(summary = "后台查看履约时间线")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<List<AppDeltaServiceOrderTimelineRespVO>> getTimeline(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<AppDeltaServiceOrderTimelineRespVO> timeline = deltaServiceOrderService.getTimeline(serviceOrderId);
        return success(timeline);
    }

    // ====== Phase 6 验收与返工 ======

    @PostMapping("/accept")
    @Operation(summary = "后台验收通过")
    @PreAuthorize("@ss.hasPermission('delta:service-order:accept')")
    public CommonResult<Boolean> accept(
            @RequestBody @Valid DeltaServiceOrderAcceptReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.acceptByAdmin(adminUserId, reqVO.getServiceOrderId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/request-rework")
    @Operation(summary = "后台要求返工")
    @PreAuthorize("@ss.hasPermission('delta:service-order:rework')")
    public CommonResult<Boolean> requestRework(
            @RequestBody @Valid DeltaServiceOrderReworkReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaServiceOrderService.requestReworkByAdmin(adminUserId, reqVO.getServiceOrderId(), reqVO.getReason());
        return success(true);
    }

    @GetMapping("/acceptance-list")
    @Operation(summary = "查询验收记录")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<List<DeltaOrderAcceptanceRespVO>> getAcceptanceList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderAcceptanceRespVO> list = deltaServiceOrderService.getAcceptanceList(serviceOrderId);
        return success(list);
    }

    @GetMapping("/rework-list")
    @Operation(summary = "查询返工记录")
    @PreAuthorize("@ss.hasPermission('delta:service-order:query')")
    public CommonResult<List<DeltaOrderReworkRespVO>> getReworkList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderReworkRespVO> list = deltaServiceOrderService.getReworkList(serviceOrderId);
        return success(list);
    }

}
