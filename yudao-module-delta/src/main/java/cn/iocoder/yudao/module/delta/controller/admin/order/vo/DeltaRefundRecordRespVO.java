package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 退款记录详情 Response VO")
@Data
public class DeltaRefundRecordRespVO {

    @Schema(description = "退款记录ID", example = "1")
    private Long id;

    @Schema(description = "退款单号", example = "DRF202406010001")
    private String refundNo;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "售后案件ID")
    private Long afterSaleId;

    @Schema(description = "买家用户ID", example = "10")
    private Long buyerUserId;

    @Schema(description = "退款金额(分)", example = "5000")
    private Integer refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "退款状态", example = "1")
    private Integer refundStatus;

    @Schema(description = "退款状态名称", example = "人工退款处理中")
    private String refundStatusName;

    @Schema(description = "退款方式", example = "1")
    private Integer refundMethod;

    @Schema(description = "退款方式名称", example = "人工微信")
    private String refundMethodName;

    @Schema(description = "外部参考号")
    private String externalReference;

    @Schema(description = "退款凭证URL")
    private String proofUrls;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "开始处理时间")
    private String handleTime;

    @Schema(description = "退款完成时间")
    private String completedTime;

    @Schema(description = "退款失败时间")
    private String failedTime;

    @Schema(description = "退款失败原因")
    private String failureReason;

    @Schema(description = "处理备注")
    private String processRemark;

    @Schema(description = "创建人ID")
    private Long operatorId;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "备注")
    private String remark;

}
