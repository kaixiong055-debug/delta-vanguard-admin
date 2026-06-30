package cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 打手申请信息 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 打手申请信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerApplicationRespVO {

    @Schema(description = "申请ID", example = "1")
    private Long id;

    @Schema(description = "会员用户ID", example = "100")
    private Long userId;

    @Schema(description = "会员昵称", example = "老板小王")
    private String memberNickname;

    @Schema(description = "会员头像", example = "https://cdn.example.com/avatar.jpg")
    private String memberAvatar;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "游戏账号UID", example = "123456789")
    private String gameUid;

    @Schema(description = "设备类型：1-手机 2-平板 3-PC", example = "1")
    private Integer deviceType;

    @Schema(description = "个人介绍", example = "擅长各种游戏")
    private String introduction;

    @Schema(description = "打手经验描述", example = "5年经验")
    private String experience;

    @Schema(description = "证明材料图片URL列表")
    private List<String> evidenceUrls;

    @Schema(description = "审核凭证图片URL", example = "https://cdn.example.com/check.jpg")
    private String checkEvidenceUrl;

    @Schema(description = "申请状态：0-未申请 1-审核中 2-审核通过 3-审核驳回", example = "1")
    private Integer applicationStatus;

    @Schema(description = "驳回原因", example = "证明材料不完整")
    private String rejectReason;

    @Schema(description = "审核人ID", example = "1")
    private Long reviewerId;

    @Schema(description = "审核时间", example = "2023-01-01 12:00:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "创建时间", example = "2023-01-01 10:00:00")
    private LocalDateTime createTime;

}
