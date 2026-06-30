package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单状态分布响应项 VO
 */
@Schema(description = "管理后台 - 订单状态分布")
@Data
public class DeltaStatisticsOrderStatusDistributionRespVO {

    @Schema(description = "状态值")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "订单数量")
    private Long count;

    @Schema(description = "订单金额（分）")
    private Long amount;

}
