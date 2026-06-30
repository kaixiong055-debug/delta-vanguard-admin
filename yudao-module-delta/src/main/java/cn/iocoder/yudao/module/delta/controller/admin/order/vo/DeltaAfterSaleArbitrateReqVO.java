package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 仲裁售后 Request VO
 */
@Schema(description = "管理后台 - 仲裁售后 Request VO")
@Data
public class DeltaAfterSaleArbitrateReqVO {

    @Schema(description = "售后案件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "售后案件ID不能为空")
    private Long id;

    @Schema(description = "仲裁决定：0-不退款 1-全额退款 2-部分退款 3-继续服务", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "仲裁决定不能为空")
    private Integer decisionType;

    @Schema(description = "退款金额（分）", example = "3000")
    private Integer refundAmount;

    @Schema(description = "责任归属：0-买家 1-打手 2-平台 3-共同 4-无", example = "2")
    private Integer responsibilityType;

    @Schema(description = "打手扣减金额（分）", example = "2000")
    private Integer workerDeductionAmount;

    @Schema(description = "平台承担金额（分）", example = "1000")
    private Integer platformBearAmount;

    @Schema(description = "备注", example = "平台仲裁部分退款")
    private String remark;

}
