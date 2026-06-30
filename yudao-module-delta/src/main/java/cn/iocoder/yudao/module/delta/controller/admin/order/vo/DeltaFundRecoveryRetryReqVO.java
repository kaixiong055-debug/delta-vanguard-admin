package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 重试追回 Request VO")
@Data
public class DeltaFundRecoveryRetryReqVO {

    @Schema(description = "追回任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "备注")
    private String remark;

}
