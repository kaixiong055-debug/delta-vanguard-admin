package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台 - 打手结算响应 VO（分页列表用）
 */
@Schema(description = "管理后台 - 打手结算 Response VO")
@Data
public class DeltaWorkerSettlementRespVO {

    @Schema(description = "结算ID", example = "1")
    private Long id;

    @Schema(description = "结算单号", example = "DSU202606220001")
    private String settlementNo;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "服务订单号", example = "DSO202606010001")
    private String serviceOrderNo;

    @Schema(description = "打手ID", example = "10")
    private Long workerId;

    @Schema(description = "打手名称", example = "张三")
    private String workerName;

    @Schema(description = "服务金额（分）", example = "10000")
    private Integer serviceAmount;

    @Schema(description = "抽成比例（万分制）", example = "1500")
    private Integer commissionRate;

    @Schema(description = "平台抽成金额（分）", example = "1500")
    private Integer platformFee;

    @Schema(description = "打手收入金额（分）", example = "8500")
    private Integer workerAmount;

    @Schema(description = "结算状态", example = "1")
    private Integer settlementStatus;

    @Schema(description = "结算状态名称", example = "审核通过")
    private String settlementStatusName;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "打款时间")
    private LocalDateTime paidTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
