package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 打手排行请求 VO
 */
@Schema(description = "管理后台 - 打手排行请求")
@Data
public class DeltaStatisticsWorkerRankingReqVO {

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "排行类型：ORDER_COUNT/COMPLETED_COUNT/SERVICE_AMOUNT/SETTLEMENT_AMOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排行类型不能为空")
    private String rankingType;

    @Schema(description = "返回条数，默认10")
    @Min(1)
    @Max(100)
    private Integer limit;

    public Integer getLimit() {
        return limit != null ? limit : 10;
    }

}
