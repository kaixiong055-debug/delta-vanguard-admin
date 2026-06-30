package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台 - 重新提交审核请求 VO
 */
@Schema(description = "管理后台 - 重新提交审核 Request VO")
@Data
public class DeltaWorkerSettlementResubmitReqVO {

    @Schema(description = "结算ID", required = true, example = "1")
    @NotNull(message = "结算ID不能为空")
    private Long id;

    @Schema(description = "抽成比例（万分制，修改时传入）", example = "1500")
    private Integer commissionRate;

    @Schema(description = "备注", example = "已修正抽成比例")
    private String remark;

}
