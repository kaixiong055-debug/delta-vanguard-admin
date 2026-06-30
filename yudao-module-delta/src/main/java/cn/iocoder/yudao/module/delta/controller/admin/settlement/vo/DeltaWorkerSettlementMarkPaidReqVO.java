package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台 - 标记已打款请求 VO
 */
@Schema(description = "管理后台 - 标记已打款 Request VO")
@Data
public class DeltaWorkerSettlementMarkPaidReqVO {

    @Schema(description = "结算ID", required = true, example = "1")
    @NotNull(message = "结算ID不能为空")
    private Long id;

    @Schema(description = "打款方式", example = "1")
    private Integer paymentMethod;

    @Schema(description = "打款参考号（人工转账流水号）", example = "WX20260622001")
    private String paymentReference;

    @Schema(description = "打款备注", example = "2026-06-22 人工转账")
    private String remark;

}
