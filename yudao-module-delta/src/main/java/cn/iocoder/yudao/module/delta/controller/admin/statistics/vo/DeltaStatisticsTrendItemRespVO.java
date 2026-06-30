package cn.iocoder.yudao.module.delta.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运营趋势单项响应 VO
 */
@Schema(description = "管理后台 - 运营趋势单项")
@Data
public class DeltaStatisticsTrendItemRespVO {

    @Schema(description = "日期（格式取决于粒度：YYYY-MM-DD / YYYY-MM）")
    private String date;

    @Schema(description = "服务订单数")
    private Long orderCount;

    @Schema(description = "已完成订单数")
    private Long completedOrderCount;

    @Schema(description = "服务金额（分）")
    private Long serviceAmount;

    @Schema(description = "已完成服务金额（分）")
    private Long completedServiceAmount;

    @Schema(description = "售后案件数")
    private Long afterSaleCount;

    @Schema(description = "退款金额（分）")
    private Long refundAmount;

    @Schema(description = "已追回金额（分）")
    private Long recoveredAmount;

}
