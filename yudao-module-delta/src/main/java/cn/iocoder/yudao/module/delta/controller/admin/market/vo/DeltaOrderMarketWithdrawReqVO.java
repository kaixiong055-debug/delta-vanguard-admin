package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 撤回挂牌 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 撤回挂牌 Request VO")
@Data
public class DeltaOrderMarketWithdrawReqVO {

    @Schema(description = "挂牌ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "挂牌ID不能为空")
    private Long id;

    @Schema(description = "撤回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "订单信息有误")
    @NotBlank(message = "撤回原因不能为空")
    private String reason;
}
