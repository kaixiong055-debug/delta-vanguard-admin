package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 对账记录 Excel 导出请求 VO
 */
@Schema(description = "管理后台 - 对账记录 Excel 导出请求")
@Data
public class DeltaFinanceReconciliationExportReqVO {

    @Schema(description = "对账单号")
    private String reconciliationNo;

    @Schema(description = "对账日期")
    private LocalDate reconciliationDate;

    @Schema(description = "状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
