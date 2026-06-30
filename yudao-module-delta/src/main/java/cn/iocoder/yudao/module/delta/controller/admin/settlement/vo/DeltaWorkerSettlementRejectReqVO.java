package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 后台 - 审核驳回请求 VO
 */
@Schema(description = "管理后台 - 审核驳回 Request VO")
@Data
public class DeltaWorkerSettlementRejectReqVO {

    @Schema(description = "结算ID", required = true, example = "1")
    @NotNull(message = "结算ID不能为空")
    private Long id;

    @Schema(description = "驳回原因", required = true, example = "抽成比例配置错误")
    @NotBlank(message = "驳回原因不能为空")
    private String reason;

}
