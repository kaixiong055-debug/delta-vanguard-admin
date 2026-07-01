package cn.iocoder.yudao.module.delta.controller.app.market.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户 App - 订单市场分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppDeltaOrderMarketPageReqVO extends PageParam {
}
