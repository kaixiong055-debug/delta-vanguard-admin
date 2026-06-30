package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 撤销退款记录 Request VO")
@Data
public class DeltaRefundRecordCancelReqVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "撤销原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "仲裁决定被管理员纠正")
    private String reason;

}
