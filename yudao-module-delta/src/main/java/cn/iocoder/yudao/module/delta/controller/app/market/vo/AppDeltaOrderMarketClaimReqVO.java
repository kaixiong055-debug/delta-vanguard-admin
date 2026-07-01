package cn.iocoder.yudao.module.delta.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 订单市场抢单 Request VO")
@Data
public class AppDeltaOrderMarketClaimReqVO {

    @Schema(description = "挂牌 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "挂牌 ID 不能为空")
    private Long id;
}
