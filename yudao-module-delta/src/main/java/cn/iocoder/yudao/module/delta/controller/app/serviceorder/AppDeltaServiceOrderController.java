package cn.iocoder.yudao.module.delta.controller.app.serviceorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.*;
import cn.iocoder.yudao.module.delta.convert.order.DeltaOrderEvidenceConvert;
import cn.iocoder.yudao.module.delta.convert.order.DeltaOrderProgressConvert;
import cn.iocoder.yudao.module.delta.convert.order.DeltaServiceOrderConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.service.order.DeltaServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App - 服务履约订单 Controller
 */
@Tag(name = "用户 App - 服务履约订单")
@RestController
@RequestMapping("/app-api/delta/service-order")
@Validated
public class AppDeltaServiceOrderController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;

    // ====== Phase 3 已有 ======

    @GetMapping("/page")
    @Operation(summary = "分页查询我的服务履约订单")
    public CommonResult<PageResult<AppDeltaServiceOrderRespVO>> getServiceOrderPage(
            @Valid AppDeltaServiceOrderPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<DeltaServiceOrderDO> pageResult = deltaServiceOrderService.getServiceOrderPage(userId, pageReqVO);
        return success(DeltaServiceOrderConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "查询我的服务履约订单详情")
    public CommonResult<AppDeltaServiceOrderDetailRespVO> getServiceOrderDetail(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        DeltaServiceOrderDO order = deltaServiceOrderService.getServiceOrderForUser(id, userId);
        return success(DeltaServiceOrderConvert.INSTANCE.convertDetail(order));
    }

    // ====== Phase 5 老板查询履约进度 ======

    @GetMapping("/progress-list")
    @Operation(summary = "买家查询服务单履约进度")
    public CommonResult<List<AppDeltaServiceOrderProgressRespVO>> getProgressList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        List<DeltaOrderProgressDO> list = deltaServiceOrderService.getBuyerProgressList(userId, serviceOrderId);
        List<AppDeltaServiceOrderProgressRespVO> voList = DeltaOrderProgressConvert.INSTANCE.convertListForBuyer(list);
        voList.forEach(DeltaOrderProgressConvert.INSTANCE::fillProgressTypeNameForBuyer);
        return success(voList);
    }

    @GetMapping("/evidence-list")
    @Operation(summary = "买家查询服务单凭证")
    public CommonResult<List<AppDeltaServiceOrderEvidenceRespVO>> getEvidenceList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        List<DeltaOrderEvidenceDO> list = deltaServiceOrderService.getBuyerEvidenceList(userId, serviceOrderId);
        List<AppDeltaServiceOrderEvidenceRespVO> voList = DeltaOrderEvidenceConvert.INSTANCE.convertListForBuyer(list);
        voList.forEach(DeltaOrderEvidenceConvert.INSTANCE::fillEvidenceTypeNameForBuyer);
        return success(voList);
    }

    @GetMapping("/timeline")
    @Operation(summary = "买家查询服务单履约时间线")
    public CommonResult<List<AppDeltaServiceOrderTimelineRespVO>> getTimeline(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        List<AppDeltaServiceOrderTimelineRespVO> timeline = deltaServiceOrderService.getBuyerTimeline(userId, serviceOrderId);
        return success(timeline);
    }

    // ====== Phase 6 验收与返工 ======

    @PostMapping("/accept")
    @Operation(summary = "老板验收通过")
    public CommonResult<Boolean> accept(
            @RequestBody @Valid AppDeltaServiceOrderAcceptReqVO reqVO) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.acceptByBuyer(userId, reqVO.getServiceOrderId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/request-rework")
    @Operation(summary = "老板要求返工")
    public CommonResult<Boolean> requestRework(
            @RequestBody @Valid AppDeltaServiceOrderReworkReqVO reqVO) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.requestReworkByBuyer(userId, reqVO.getServiceOrderId(), reqVO.getReason());
        return success(true);
    }

    @GetMapping("/acceptance-list")
    @Operation(summary = "查询验收记录")
    public CommonResult<List<DeltaOrderAcceptanceRespVO>> getAcceptanceList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderAcceptanceRespVO> list = deltaServiceOrderService.getAcceptanceList(serviceOrderId);
        return success(list);
    }

    @GetMapping("/rework-list")
    @Operation(summary = "查询返工记录")
    public CommonResult<List<DeltaOrderReworkRespVO>> getReworkList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        List<DeltaOrderReworkRespVO> list = deltaServiceOrderService.getReworkList(serviceOrderId);
        return success(list);
    }

    // ====== Phase 8 取消申请 ======

    @PostMapping("/cancel-apply")
    @Operation(summary = "提交取消申请")
    public CommonResult<Long> applyCancel(
            @RequestBody @Valid AppDeltaCancelApplyReqVO reqVO) {
        Long userId = getLoginUserId();
        Long cancelId = deltaServiceOrderService.applyCancelByBuyer(userId,
                reqVO.getServiceOrderId(), reqVO.getReason(), reqVO.getRemark());
        return success(cancelId);
    }

    @GetMapping("/cancel-list")
    @Operation(summary = "查询我的取消申请列表")
    public CommonResult<PageResult<AppDeltaCancelRespVO>> getCancelList(
            @Valid cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
            @RequestParam(value = "status", required = false) Integer status) {
        Long userId = getLoginUserId();
        PageResult<AppDeltaCancelRespVO> result = deltaServiceOrderService.getBuyerCancelPage(userId, pageParam);
        return success(result);
    }

    @GetMapping("/cancel-get")
    @Operation(summary = "查询取消申请详情")
    public CommonResult<AppDeltaCancelRespVO> getCancelDetail(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        AppDeltaCancelRespVO vo = deltaServiceOrderService.getBuyerCancelDetail(userId, id);
        return success(vo);
    }

    // ====== Phase 8 售后申请 ======

    @PostMapping("/after-sale-apply")
    @Operation(summary = "提交售后申请")
    public CommonResult<Long> applyAfterSale(
            @RequestBody @Valid AppDeltaAfterSaleApplyReqVO reqVO) {
        Long userId = getLoginUserId();
        Long afterSaleId = deltaServiceOrderService.applyAfterSaleByBuyer(userId,
                reqVO.getServiceOrderId(), reqVO.getAfterSaleType(),
                reqVO.getReasonType(), reqVO.getReason(),
                reqVO.getDescription(), reqVO.getRequestedRefundAmount(), reqVO.getEvidenceUrls());
        return success(afterSaleId);
    }

    @GetMapping("/after-sale-page")
    @Operation(summary = "查询我的售后案件列表")
    public CommonResult<PageResult<AppDeltaAfterSaleRespVO>> getAfterSalePage(
            @Valid cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
            @RequestParam(value = "status", required = false) Integer status) {
        Long userId = getLoginUserId();
        PageResult<AppDeltaAfterSaleRespVO> result = deltaServiceOrderService.getBuyerAfterSalePage(userId, status, pageParam);
        return success(result);
    }

    @GetMapping("/after-sale-get")
    @Operation(summary = "查询售后案件详情")
    public CommonResult<AppDeltaAfterSaleRespVO> getAfterSaleDetail(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        AppDeltaAfterSaleRespVO vo = deltaServiceOrderService.getBuyerAfterSaleDetail(userId, id);
        return success(vo);
    }

    // ====== Phase 9 退款记录查询 ======

    @GetMapping("/refund-list")
    @Operation(summary = "查询我的退款记录")
    public CommonResult<PageResult<AppDeltaRefundRespVO>> getRefundList(
            @Valid cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        Long userId = getLoginUserId();
        PageResult<AppDeltaRefundRespVO> result = deltaServiceOrderService.getBuyerRefundPage(userId, pageParam);
        return success(result);
    }

    @GetMapping("/refund-get")
    @Operation(summary = "查询退款记录详情")
    public CommonResult<AppDeltaRefundRespVO> getRefundDetail(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        AppDeltaRefundRespVO vo = deltaServiceOrderService.getBuyerRefundDetail(userId, id);
        return success(vo);
    }

}
