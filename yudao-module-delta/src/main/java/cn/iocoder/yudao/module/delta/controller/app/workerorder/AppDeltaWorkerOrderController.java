package cn.iocoder.yudao.module.delta.controller.app.workerorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.vo.AppDeltaOrderPoolRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.*;
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
 * App - 打手订单 Controller
 */
@Tag(name = "用户 App - 我的打手订单")
@RestController
@RequestMapping("/delta/worker-order")
@Validated
public class AppDeltaWorkerOrderController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;

    // ====== Phase 4 已有 ======

    @GetMapping("/page")
    @Operation(summary = "分页查询我的打手订单")
    public CommonResult<PageResult<AppDeltaOrderPoolRespVO>> getWorkerOrderPage(
            @Valid AppDeltaWorkerOrderPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        PageResult<DeltaServiceOrderDO> pageResult = deltaServiceOrderService.getWorkerOrderPage(
                userId, pageReqVO.getStatus(), pageReqVO);
        return success(DeltaServiceOrderConvert.INSTANCE.convertPoolPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "查询我的打手订单详情")
    public CommonResult<AppDeltaWorkerOrderDetailRespVO> getWorkerOrderDetail(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        DeltaServiceOrderDO order = deltaServiceOrderService.getWorkerOrderDetail(id, userId);
        return success(DeltaServiceOrderConvert.INSTANCE.convertWorkerOrderDetail(order));
    }

    // ====== Phase 5 打手服务执行 ======

    @PostMapping("/start")
    @Operation(summary = "打手开始服务")
    public CommonResult<Boolean> startService(@RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.startService(userId, serviceOrderId);
        return success(true);
    }

    @PostMapping("/progress")
    @Operation(summary = "提交服务进度")
    public CommonResult<AppDeltaWorkerOrderProgressRespVO> createProgress(
            @RequestBody @Valid AppDeltaWorkerOrderProgressCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        DeltaOrderProgressDO progress = deltaServiceOrderService.createProgress(userId, reqVO);
        AppDeltaWorkerOrderProgressRespVO respVO = DeltaOrderProgressConvert.INSTANCE.convert(progress);
        DeltaOrderProgressConvert.INSTANCE.fillProgressTypeName(respVO);
        return success(respVO);
    }

    @GetMapping("/progress-list")
    @Operation(summary = "查询服务进度列表")
    public CommonResult<List<AppDeltaWorkerOrderProgressRespVO>> getProgressList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        List<DeltaOrderProgressDO> list = deltaServiceOrderService.getWorkerProgressList(userId, serviceOrderId);
        List<AppDeltaWorkerOrderProgressRespVO> voList = DeltaOrderProgressConvert.INSTANCE.convertList(list);
        voList.forEach(DeltaOrderProgressConvert.INSTANCE::fillProgressTypeName);
        return success(voList);
    }

    @PostMapping("/evidence")
    @Operation(summary = "登记服务凭证")
    public CommonResult<AppDeltaWorkerOrderEvidenceRespVO> createEvidence(
            @RequestBody @Valid AppDeltaWorkerOrderEvidenceCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        DeltaOrderEvidenceDO evidence = deltaServiceOrderService.createEvidence(userId, reqVO);
        AppDeltaWorkerOrderEvidenceRespVO respVO = DeltaOrderEvidenceConvert.INSTANCE.convert(evidence);
        DeltaOrderEvidenceConvert.INSTANCE.fillEvidenceTypeName(respVO);
        return success(respVO);
    }

    @DeleteMapping("/evidence/delete")
    @Operation(summary = "删除服务凭证")
    public CommonResult<Boolean> deleteEvidence(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.deleteEvidence(userId, id);
        return success(true);
    }

    @GetMapping("/evidence-list")
    @Operation(summary = "查询服务凭证列表")
    public CommonResult<List<AppDeltaWorkerOrderEvidenceRespVO>> getEvidenceList(
            @RequestParam("serviceOrderId") Long serviceOrderId) {
        Long userId = getLoginUserId();
        List<DeltaOrderEvidenceDO> list = deltaServiceOrderService.getWorkerEvidenceList(userId, serviceOrderId);
        List<AppDeltaWorkerOrderEvidenceRespVO> voList = DeltaOrderEvidenceConvert.INSTANCE.convertList(list);
        voList.forEach(DeltaOrderEvidenceConvert.INSTANCE::fillEvidenceTypeName);
        return success(voList);
    }

    @PostMapping("/submit-completion")
    @Operation(summary = "提交服务完成")
    public CommonResult<Boolean> submitCompletion(
            @RequestBody @Valid AppDeltaWorkerOrderSubmitCompletionReqVO reqVO) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.submitCompletion(userId, reqVO.getServiceOrderId(), reqVO.getSummary());
        return success(true);
    }

}
