package cn.iocoder.yudao.module.delta.controller.admin.finance;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.delta.convert.finance.DeltaFinanceReconciliationConvert;
import cn.iocoder.yudao.module.delta.service.finance.DeltaFinanceReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 财务对账 Controller
 */
@Tag(name = "管理后台 - 财务对账")
@RestController
@RequestMapping("/delta/finance-reconciliation")
@Validated
public class DeltaFinanceReconciliationController {

    @Resource
    private DeltaFinanceReconciliationService deltaFinanceReconciliationService;

    @GetMapping("/page")
    @Operation(summary = "对账记录分页")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:query')")
    public CommonResult<PageResult<DeltaFinanceReconciliationRespVO>> page(
            @Valid DeltaFinanceReconciliationPageReqVO reqVO) {
        return success(deltaFinanceReconciliationService.getReconciliationPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "对账记录详情")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:query')")
    public CommonResult<DeltaFinanceReconciliationRespVO> get(
            @RequestParam("id") Long id) {
        return success(deltaFinanceReconciliationService.getReconciliation(id));
    }

    @PostMapping("/generate")
    @Operation(summary = "生成对账")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:generate')")
    public CommonResult<Long> generate(
            @RequestBody @Valid DeltaFinanceReconciliationGenerateReqVO reqVO) {
        return success(deltaFinanceReconciliationService.generateReconciliation(reqVO));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认对账")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:confirm')")
    public CommonResult<Boolean> confirm(
            @RequestBody @Valid DeltaFinanceReconciliationConfirmReqVO reqVO) {
        deltaFinanceReconciliationService.confirmReconciliation(reqVO);
        return success(true);
    }

    @PostMapping("/retry")
    @Operation(summary = "重试对账")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:retry')")
    public CommonResult<Boolean> retry(
            @RequestParam("id") Long id) {
        deltaFinanceReconciliationService.retryReconciliation(id);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消对账")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:cancel')")
    public CommonResult<Boolean> cancel(
            @RequestBody @Valid DeltaFinanceReconciliationCancelReqVO reqVO) {
        deltaFinanceReconciliationService.cancelReconciliation(reqVO);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出对账 Excel")
    @PreAuthorize("@ss.hasPermission('delta:finance-reconciliation:export')")
    public void exportExcel(
            @Valid DeltaFinanceReconciliationExportReqVO reqVO,
            HttpServletResponse response) throws IOException {
        List<DeltaFinanceReconciliationRespVO> list =
                deltaFinanceReconciliationService.getReconciliationList(reqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, buildFilename(), "对账记录",
                    DeltaFinanceReconciliationExcelVO.class, new ArrayList<>());
            return;
        }
        List<DeltaFinanceReconciliationExcelVO> excelList =
                DeltaFinanceReconciliationConvert.INSTANCE.convertToExcelList(list);
        ExcelUtils.write(response, buildFilename(), "对账记录",
                DeltaFinanceReconciliationExcelVO.class, excelList);
    }

    private String buildFilename() {
        return "Delta财务对账_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
    }

}
