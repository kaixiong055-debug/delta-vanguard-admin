package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 俱乐部抢单 Request VO（俱乐部身份由当前 tenantId 确定）
 *
 * @author Delta-Vanguard
 */
@Schema(description = "俱乐部 - 抢单 Request VO")
@Data
public class DeltaOrderMarketClaimReqVO {

    @Schema(description = "挂牌ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @javax.validation.constraints.NotNull(message = "挂牌ID不能为空")
    private Long id;
}
