package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 重新处理退款 Request VO")
@Data
public class DeltaRefundRecordRetryReqVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "备注", example = "买家已更新收款信息")
    private String remark;

}
