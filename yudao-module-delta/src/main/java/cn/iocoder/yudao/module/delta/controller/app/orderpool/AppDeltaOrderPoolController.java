package cn.iocoder.yudao.module.delta.controller.app.orderpool;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.vo.AppDeltaOrderPoolClaimReqVO;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.vo.AppDeltaOrderPoolPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.vo.AppDeltaOrderPoolRespVO;
import cn.iocoder.yudao.module.delta.convert.order.DeltaServiceOrderConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.service.order.DeltaServiceOrderService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App - 订单池 Controller
 */
@Tag(name = "用户 App - 订单池")
@RestController
@RequestMapping("/delta/order-pool")
@Validated
public class AppDeltaOrderPoolController {

    @Resource
    private DeltaServiceOrderService deltaServiceOrderService;
    @Resource
    private DeltaWorkerService deltaWorkerService;

    @GetMapping("/page")
    @Operation(summary = "订单池分页查询")
    public CommonResult<PageResult<AppDeltaOrderPoolRespVO>> getPoolPage(
            @Valid AppDeltaOrderPoolPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = getCurrentWorker(userId);
        PageResult<DeltaServiceOrderDO> pageResult = deltaServiceOrderService.getPoolPage(
                worker.getId(), pageReqVO.getDeviceType(), pageReqVO.getServiceType(), pageReqVO);
        return success(DeltaServiceOrderConvert.INSTANCE.convertPoolPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "订单池详情查询")
    public CommonResult<AppDeltaOrderPoolRespVO> getPoolDetail(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = getCurrentWorker(userId);
        DeltaServiceOrderDO order = deltaServiceOrderService.getPoolDetail(id, worker.getId());
        return success(DeltaServiceOrderConvert.INSTANCE.convertPoolDetail(order));
    }

    @PostMapping("/claim")
    @Operation(summary = "打手接单")
    public CommonResult<Boolean> claimOrder(@RequestBody @Valid AppDeltaOrderPoolClaimReqVO reqVO) {
        Long userId = getLoginUserId();
        deltaServiceOrderService.claimOrder(userId, reqVO.getServiceOrderId());
        return success(true);
    }

    private DeltaWorkerDO getCurrentWorker(Long userId) {
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(userId);
        if (worker == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil
                    .exception(cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        return worker;
    }

}
