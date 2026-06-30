package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台 - 补生成结算请求 VO
 */
@Schema(description = "管理后台 - 补生成结算 Request VO")
@Data
public class DeltaWorkerSettlementGenerateReqVO {

    @Schema(description = "服务订单ID", required = true, example = "100")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

}
