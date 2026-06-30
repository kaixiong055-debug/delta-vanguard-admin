package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 APP - 打手身份 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 打手身份 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppDeltaWorkerIdentityRespVO {

    @Schema(description = "是否已是打手", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean isWorker;

    @Schema(description = "审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer auditStatus;

    @Schema(description = "最近申请状态：0-未申请 1-审核中 2-审核通过 3-审核驳回",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer applicationStatus;

    @Schema(description = "工作状态：0-离线 1-在线 2-忙碌 3-暂停接单", example = "0")
    private Integer workStatus;

    @Schema(description = "是否启用（状态：0-开启 1-关闭）", example = "0")
    private Integer enabledStatus;

    @Schema(description = "打手ID", example = "1")
    private Long workerId;

    @Schema(description = "打手编号", example = "DW20230101120000001")
    private String workerNo;

    @Schema(description = "展示名称", example = "小宋")
    private String displayName;

    @Schema(description = "驳回原因（最近一次申请）", example = "证明材料不完整")
    private String rejectReason;

}
