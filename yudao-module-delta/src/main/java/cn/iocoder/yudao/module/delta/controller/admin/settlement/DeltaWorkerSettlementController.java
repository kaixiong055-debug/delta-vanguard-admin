package cn.iocoder.yudao.module.delta.controller.admin.settlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.settlement.vo.*;
import cn.iocoder.yudao.module.delta.convert.settlement.DeltaWorkerSettlementConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementLogService;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementPhase7CoreService;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 后台 - 打手结算 Controller
 */
@Tag(name = "管理后台 - 打手结算")
@RestController
@RequestMapping("/delta/worker-settlement")
@Validated
public class DeltaWorkerSettlementController {

    @Resource
    private DeltaWorkerSettlementService deltaWorkerSettlementService;
    @Resource
    private DeltaWorkerSettlementLogService deltaWorkerSettlementLogService;
    @Resource
    private DeltaWorkerSettlementPhase7CoreService deltaWorkerSettlementPhase7CoreService;
    @Resource
    private DeltaWorkerService deltaWorkerService;

    // ====== 查询 ======

    @GetMapping("/page")
    @Operation(summary = "后台分页查询结算列表")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:query')")
    public CommonResult<PageResult<DeltaWorkerSettlementRespVO>> page(
            @Valid DeltaWorkerSettlementPageReqVO pageReqVO) {
        DeltaWorkerSettlementMapper mapper = deltaWorkerSettlementService.getMapper();
        PageResult<DeltaWorkerSettlementDO> pageResult = mapper.selectPage(pageReqVO);
        List<DeltaWorkerSettlementRespVO> voList = DeltaWorkerSettlementConvert.INSTANCE.convertListAdmin(pageResult.getList());
        // 填充状态名称和打手名称
        Map<Long, String> workerNameMap = buildWorkerNameMap(pageResult.getList());
        voList.forEach(vo -> {
            DeltaWorkerSettlementConvert.INSTANCE.fillStatusNameAdmin(vo);
            if (vo.getWorkerId() != null) {
                vo.setWorkerName(workerNameMap.getOrDefault(vo.getWorkerId(), "打手" + vo.getWorkerId()));
            }
        });
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "后台查询结算详情")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:query')")
    public CommonResult<DeltaWorkerSettlementDetailRespVO> get(
            @RequestParam("id") Long id) {
        DeltaWorkerSettlementDO settlement = deltaWorkerSettlementService.getSettlement(id);
        if (settlement == null) {
            return success(null);
        }
        DeltaWorkerSettlementDetailRespVO vo = DeltaWorkerSettlementConvert.INSTANCE.convertDetail(settlement);
        DeltaWorkerSettlementConvert.INSTANCE.fillStatusNameDetail(vo);
        // 填充打手名称
        if (vo.getWorkerId() != null) {
            DeltaWorkerDO worker = deltaWorkerService.getWorker(vo.getWorkerId());
            if (worker != null) {
                vo.setWorkerName(worker.getDisplayName() != null ? worker.getDisplayName() : worker.getRealName());
            }
        }
        // 查询结算日志
        List<DeltaWorkerSettlementLogDO> logs = deltaWorkerSettlementLogService.getLogsBySettlementId(id);
        vo.setLogs(DeltaWorkerSettlementConvert.INSTANCE.convertLogItems(logs));
        return success(vo);
    }

    // ====== 操作 ======

    @PostMapping("/generate")
    @Operation(summary = "后台补生成结算")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:generate')")
    public CommonResult<Boolean> generate(
            @RequestBody @Valid DeltaWorkerSettlementGenerateReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.generateSettlementByAdmin(adminUserId, reqVO.getServiceOrderId());
        return success(true);
    }

    @PostMapping("/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:approve')")
    public CommonResult<Boolean> approve(
            @RequestBody @Valid DeltaWorkerSettlementApproveReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.approveSettlement(adminUserId, reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/reject")
    @Operation(summary = "审核驳回")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:reject')")
    public CommonResult<Boolean> reject(
            @RequestBody @Valid DeltaWorkerSettlementRejectReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.rejectSettlement(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

    @PostMapping("/resubmit")
    @Operation(summary = "重新提交审核")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:approve')")
    public CommonResult<Boolean> resubmit(
            @RequestBody @Valid DeltaWorkerSettlementResubmitReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.resubmitSettlement(adminUserId, reqVO.getId(),
                reqVO.getCommissionRate(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/mark-paid")
    @Operation(summary = "标记已打款")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:pay')")
    public CommonResult<Boolean> markPaid(
            @RequestBody @Valid DeltaWorkerSettlementMarkPaidReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.markSettlementPaid(adminUserId, reqVO.getId(),
                reqVO.getPaymentMethod(), reqVO.getPaymentReference(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/revoke-paid")
    @Operation(summary = "撤销打款标记")
    @PreAuthorize("@ss.hasPermission('delta:worker-settlement:pay')")
    public CommonResult<Boolean> revokePaid(
            @RequestBody @Valid DeltaWorkerSettlementRevokePaidReqVO reqVO) {
        Long adminUserId = WebFrameworkUtils.getLoginUserId();
        deltaWorkerSettlementPhase7CoreService.revokeSettlementPaid(adminUserId, reqVO.getId(), reqVO.getReason());
        return success(true);
    }

    // ====== 辅助方法 ======

    private Map<Long, String> buildWorkerNameMap(List<DeltaWorkerSettlementDO> list) {
        java.util.Set<Long> workerIds = list.stream()
                .map(DeltaWorkerSettlementDO::getWorkerId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        return workerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            DeltaWorkerDO worker = deltaWorkerService.getWorker(id);
                            if (worker != null && worker.getDisplayName() != null) {
                                return worker.getDisplayName();
                            }
                            return "打手" + id;
                        },
                        (a, b) -> a
                ));
    }

}
