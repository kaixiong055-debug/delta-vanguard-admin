package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户 APP - 打手申请信息 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 打手申请信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppDeltaWorkerApplicationRespVO {

    @Schema(description = "申请ID", example = "1")
    private Long id;

    @Schema(description = "申请状态：0-未申请 1-审核中 2-审核通过 3-审核驳回", example = "1")
    private Integer applicationStatus;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "游戏账号UID", example = "123456789")
    private String gameUid;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "个人介绍", example = "擅长各种游戏")
    private String introduction;

    @Schema(description = "经验描述", example = "5年经验")
    private String experience;

    @Schema(description = "驳回原因", example = "证明材料不完整")
    private String rejectReason;

    @Schema(description = "审核时间", example = "2023-01-01 12:00:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "创建时间", example = "2023-01-01 10:00:00")
    private LocalDateTime createTime;

}
