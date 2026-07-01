package cn.iocoder.yudao.module.delta.controller.app.settlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.settlement.vo.AppDeltaWorkerSettlementPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.settlement.vo.AppDeltaWorkerSettlementRespVO;
import cn.iocoder.yudao.module.delta.controller.app.settlement.vo.AppDeltaWorkerSettlementSummaryRespVO;
import cn.iocoder.yudao.module.delta.convert.settlement.DeltaWorkerSettlementConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * App - 打手结算 Controller
 */
@Tag(name = "用户 App - 打手结算")
@RestController
@RequestMapping("/delta/worker-settlement")
@Validated
public class AppDeltaWorkerSettlementController {

    @Resource
    private DeltaWorkerSettlementService deltaWorkerSettlementService;
    @Resource
    private DeltaWorkerService deltaWorkerService;

    @GetMapping("/page")
    @Operation(summary = "打手查询结算分页")
    public CommonResult<PageResult<AppDeltaWorkerSettlementRespVO>> page(
            @Valid AppDeltaWorkerSettlementPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = getCurrentWorker(userId);
        PageResult<DeltaWorkerSettlementDO> pageResult = deltaWorkerSettlementService
                .getSettlementPageByWorker(worker.getId(), pageReqVO.getStatus(), pageReqVO);
        List<AppDeltaWorkerSettlementRespVO> voList = DeltaWorkerSettlementConvert.INSTANCE.convertListApp(pageResult.getList());
        voList.forEach(DeltaWorkerSettlementConvert.INSTANCE::fillStatusName);
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "打手查询结算详情")
    public CommonResult<AppDeltaWorkerSettlementRespVO> get(
            @RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = getCurrentWorker(userId);
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(id);
        if (settlement == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil
                    .exception(SETTLEMENT_NOT_EXISTS);
        }
        if (!worker.getId().equals(settlement.getWorkerId())) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil
                    .exception(SETTLEMENT_NOT_BELONG_TO_WORKER);
        }
        AppDeltaWorkerSettlementRespVO vo = DeltaWorkerSettlementConvert.INSTANCE.convert(settlement);
        DeltaWorkerSettlementConvert.INSTANCE.fillStatusName(vo);
        return success(vo);
    }

    @GetMapping("/summary")
    @Operation(summary = "打手结算汇总")
    public CommonResult<AppDeltaWorkerSettlementSummaryRespVO> summary() {
        Long userId = getLoginUserId();
        DeltaWorkerDO worker = getCurrentWorker(userId);
        Map<String, Object> summary = deltaWorkerSettlementService.getAmountSummaryByWorker(worker.getId());
        AppDeltaWorkerSettlementSummaryRespVO vo = new AppDeltaWorkerSettlementSummaryRespVO();
        vo.setPendingReviewAmount((Long) summary.getOrDefault("pendingReviewAmount", 0L));
        vo.setApprovedAmount((Long) summary.getOrDefault("approvedAmount", 0L));
        vo.setPaidAmount((Long) summary.getOrDefault("paidAmount", 0L));
        vo.setRejectedAmount((Long) summary.getOrDefault("rejectedAmount", 0L));
        vo.setTotalPaidAmount((Long) summary.getOrDefault("totalPaidAmount", 0L));
        return success(vo);
    }

    private DeltaWorkerDO getCurrentWorker(Long userId) {
        DeltaWorkerDO worker = deltaWorkerService.getWorkerByUserId(userId);
        if (worker == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil
                    .exception(ASSIGNMENT_NO_WORKER_IDENTITY);
        }
        return worker;
    }

}
