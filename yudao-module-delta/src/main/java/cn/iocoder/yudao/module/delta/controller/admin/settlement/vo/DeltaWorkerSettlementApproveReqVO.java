package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台 - 审核通过请求 VO
 */
@Schema(description = "管理后台 - 审核通过 Request VO")
@Data
public class DeltaWorkerSettlementApproveReqVO {

    @Schema(description = "结算ID", required = true, example = "1")
    @NotNull(message = "结算ID不能为空")
    private Long id;

    @Schema(description = "审核备注", example = "金额核对无误")
    private String remark;

}
