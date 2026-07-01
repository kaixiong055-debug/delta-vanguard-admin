package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 运营趋势请求 VO
 */
@Schema(description = "管理后台 - 运营趋势请求")
@Data
public class DeltaStatisticsTrendReqVO {

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "统计粒度：DAY-按天, WEEK-按周, MONTH-按月", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "统计粒度不能为空")
    private String granularity;

}
