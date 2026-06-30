package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 记录追回结果 Request VO")
@Data
public class DeltaFundRecoveryRecordReqVO {

    @Schema(description = "追回任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "本次追回金额(分)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer recoveredAmount;

    @Schema(description = "追回方式：1-人工微信 2-银行卡 3-支付宝 4-其他")
    private Integer recoveryMethod;

    @Schema(description = "外部参考号")
    private String externalReference;

    @Schema(description = "凭证URL列表")
    private List<String> proofUrls;

    @Schema(description = "备注")
    private String remark;

}
