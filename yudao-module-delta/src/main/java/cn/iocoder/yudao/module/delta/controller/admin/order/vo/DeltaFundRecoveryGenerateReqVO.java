package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 生成追回任务 Request VO")
@Data
public class DeltaFundRecoveryGenerateReqVO {

    @Schema(description = "售后案件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long afterSaleId;

}
