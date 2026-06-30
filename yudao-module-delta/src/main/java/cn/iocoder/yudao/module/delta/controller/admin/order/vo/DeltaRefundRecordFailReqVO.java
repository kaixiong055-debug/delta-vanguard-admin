package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 人工退款失败 Request VO")
@Data
public class DeltaRefundRecordFailReqVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "失败原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "买家提供的收款信息无效")
    private String reason;

}
