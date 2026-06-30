package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * App - 提交售后申请 Request VO
 */
@Schema(description = "App - 提交售后申请 Request VO")
@Data
public class AppDeltaAfterSaleApplyReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "售后类型：1-服务质量问题 2-服务与描述不符 3-打手未履约 4-其他", example = "1")
    private Integer afterSaleType;

    @Schema(description = "原因类型", example = "1")
    private Integer reasonType;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "服务结果与要求不一致")
    @NotNull(message = "售后申请原因不能为空")
    private String reason;

    @Schema(description = "问题描述", example = "具体问题说明")
    private String description;

    @Schema(description = "请求退款金额（分）", example = "5000")
    private Integer requestedRefundAmount;

    @Schema(description = "凭证图片URLs", example = "[\"https://...\"]")
    private String evidenceUrls;

}
