package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - 取消申请响应 VO
 */
@Schema(description = "App - 取消申请响应 VO")
@Data
public class AppDeltaCancelRespVO {

    @Schema(description = "取消申请ID", example = "1")
    private Long id;

    @Schema(description = "取消单号", example = "DCN202401010000001")
    private String cancelNo;

    @Schema(description = "服务订单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "服务订单号", example = "DSO202401010000001")
    private String serviceOrderNo;

    @Schema(description = "申请原因", example = "暂时不需要该服务")
    private String applyReason;

    @Schema(description = "取消状态：0-待审核 1-已通过 2-已驳回", example = "0")
    private Integer status;

    @Schema(description = "状态名称", example = "待审核")
    private String statusName;

    @Schema(description = "退款金额（分）", example = "5000")
    private Integer refundAmount;

    @Schema(description = "审核备注", example = "同意取消")
    private String reviewRemark;

    @Schema(description = "创建时间", example = "2024-01-01 00:00:00")
    private String createTime;

    @Schema(description = "审核时间", example = "2024-01-01 01:00:00")
    private String reviewTime;

}
