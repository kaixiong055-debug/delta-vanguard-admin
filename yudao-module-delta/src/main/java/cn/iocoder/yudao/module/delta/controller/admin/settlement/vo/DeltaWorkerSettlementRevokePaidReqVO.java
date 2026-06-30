package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 后台 - 撤销打款请求 VO
 */
@Schema(description = "管理后台 - 撤销打款 Request VO")
@Data
public class DeltaWorkerSettlementRevokePaidReqVO {

    @Schema(description = "结算ID", required = true, example = "1")
    @NotNull(message = "结算ID不能为空")
    private Long id;

    @Schema(description = "撤销原因", required = true, example = "录入了错误的打款记录")
    @NotBlank(message = "撤销原因不能为空")
    private String reason;

}
