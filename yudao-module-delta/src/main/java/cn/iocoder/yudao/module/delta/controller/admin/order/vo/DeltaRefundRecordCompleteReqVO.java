package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 人工退款完成 Request VO")
@Data
public class DeltaRefundRecordCompleteReqVO {

    @Schema(description = "退款记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退款方式：1-人工微信 2-银行卡 3-支付宝 4-其他", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer refundMethod;

    @Schema(description = "外部参考号", example = "WX202406010001")
    private String externalReference;

    @Schema(description = "凭证URL列表")
    private List<String> proofUrls;

    @Schema(description = "处理备注")
    private String remark;

}
