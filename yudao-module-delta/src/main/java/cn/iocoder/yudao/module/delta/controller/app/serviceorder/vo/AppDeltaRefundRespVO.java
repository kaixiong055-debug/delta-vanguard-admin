package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App 退款记录 Response VO（仅暴露买家可看信息）
 */
@Schema(description = "用户 App - 退款记录 Response VO")
@Data
public class AppDeltaRefundRespVO {

    @Schema(description = "退款记录ID", example = "1")
    private Long id;

    @Schema(description = "退款单号", example = "DRF202406010001")
    private String refundNo;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "退款金额(分)", example = "5000")
    private Integer refundAmount;

    @Schema(description = "退款状态", example = "2")
    private Integer refundStatus;

    @Schema(description = "退款状态名称", example = "人工退款已完成")
    private String refundStatusName;

    @Schema(description = "退款方式", example = "1")
    private Integer refundMethod;

    @Schema(description = "退款方式名称", example = "人工微信")
    private String refundMethodName;

    @Schema(description = "退款完成时间")
    private String completedTime;

    @Schema(description = "处理备注")
    private String processRemark;

    @Schema(description = "创建时间")
    private String createTime;

}
