package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 开始追回 Request VO")
@Data
public class DeltaFundRecoveryStartReqVO {

    @Schema(description = "追回任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "处理备注", example = "已联系打手处理退款责任")
    private String remark;

}
