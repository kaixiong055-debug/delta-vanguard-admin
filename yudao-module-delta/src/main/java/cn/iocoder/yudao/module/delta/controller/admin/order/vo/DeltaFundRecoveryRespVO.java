package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 追回任务详情 Response VO")
@Data
public class DeltaFundRecoveryRespVO {

    @Schema(description = "追回任务ID", example = "1")
    private Long id;

    @Schema(description = "追回单号", example = "DFR202406010001")
    private String recoveryNo;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "售后案件ID")
    private Long afterSaleId;

    @Schema(description = "仲裁记录ID")
    private Long arbitrationId;

    @Schema(description = "结算记录ID")
    private Long settlementId;

    @Schema(description = "打手ID", example = "20")
    private Long workerId;

    @Schema(description = "责任归属", example = "1")
    private Integer responsibilityType;

    @Schema(description = "应追回金额(分)", example = "5000")
    private Integer shouldRecoverAmount;

    @Schema(description = "已追回金额(分)", example = "2000")
    private Integer recoveredAmount;

    @Schema(description = "剩余金额(分)", example = "3000")
    private Integer remainingAmount;

    @Schema(description = "追回状态", example = "2")
    private Integer recoveryStatus;

    @Schema(description = "追回状态名称", example = "部分追回")
    private String recoveryStatusName;

    @Schema(description = "追回方式")
    private Integer recoveryMethod;

    @Schema(description = "外部参考号")
    private String externalReference;

    @Schema(description = "凭证URL")
    private String proofUrls;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "开始处理时间")
    private String handleTime;

    @Schema(description = "追回完成时间")
    private String completedTime;

    @Schema(description = "追回失败原因")
    private String failureReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private String createTime;

}
