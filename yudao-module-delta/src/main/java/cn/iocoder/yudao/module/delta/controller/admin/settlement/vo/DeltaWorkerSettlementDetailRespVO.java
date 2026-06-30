package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台 - 打手结算详情响应 VO（含日志）
 */
@Schema(description = "管理后台 - 打手结算详情 Response VO")
@Data
public class DeltaWorkerSettlementDetailRespVO {

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

    @Schema(description = "结算时间")
    private LocalDateTime settledAt;

    @Schema(description = "审核人ID", example = "1")
    private Long reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "打款人ID", example = "1")
    private Long payerId;

    @Schema(description = "打款时间")
    private LocalDateTime paidTime;

    @Schema(description = "打款方式", example = "1")
    private Integer payMethod;

    @Schema(description = "打款参考号")
    private String payReference;

    @Schema(description = "打款备注")
    private String payRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "结算操作日志")
    private List<SettlementLogItem> logs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 结算操作日志子项
     */
    @Data
    @Schema(description = "结算操作日志")
    public static class SettlementLogItem {
        @Schema(description = "操作类型", example = "APPROVE")
        private String operationType;

        @Schema(description = "操作前状态")
        private Integer beforeStatus;

        @Schema(description = "操作后状态")
        private Integer afterStatus;

        @Schema(description = "操作人类型")
        private Integer operatorType;

        @Schema(description = "操作人ID")
        private Long operatorId;

        @Schema(description = "操作内容")
        private String content;

        @Schema(description = "金额快照")
        private String amountSnapshot;

        @Schema(description = "操作时间")
        private LocalDateTime createTime;
    }

}
