package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 标记追回失败 Request VO")
@Data
public class DeltaFundRecoveryFailReqVO {

    @Schema(description = "追回任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "失败原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "暂时无法联系打手")
    private String reason;

}
