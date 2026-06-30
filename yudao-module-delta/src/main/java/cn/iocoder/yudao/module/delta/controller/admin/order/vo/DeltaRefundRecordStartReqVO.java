package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 开始处理退款 Request VO")
@Data
public class DeltaRefundRecordStartReqVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "处理备注", example = "已开始核对收款信息")
    private String remark;

}
