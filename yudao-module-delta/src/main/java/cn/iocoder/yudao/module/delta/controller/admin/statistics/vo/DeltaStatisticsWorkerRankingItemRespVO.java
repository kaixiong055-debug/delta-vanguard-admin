package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 打手排行项响应 VO
 */
@Schema(description = "管理后台 - 打手排行项")
@Data
public class DeltaStatisticsWorkerRankingItemRespVO {

    @Schema(description = "打手ID")
    private Long workerId;

    @Schema(description = "打手名称")
    private String workerName;

    @Schema(description = "接单数")
    private Long orderCount;

    @Schema(description = "完成数")
    private Long completedCount;

    @Schema(description = "服务金额（分）")
    private Long serviceAmount;

    @Schema(description = "结算金额（分）")
    private Long settlementAmount;

    @Schema(description = "排名")
    private Integer rank;

}
