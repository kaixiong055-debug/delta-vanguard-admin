package cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 管理后台 - 审核通过 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 审核通过 Request VO")
@Data
public class DeltaWorkerApplicationApproveReqVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    @Schema(description = "打手展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小宋")
    @NotNull(message = "展示名称不能为空")
    @Size(max = 50, message = "展示名称长度不能超过50个字符")
    private String displayName;

    @Schema(description = "打手等级：1-初级 2-中级 3-高级 4-资深", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打手等级不能为空")
    private Integer level;

    @Schema(description = "抽成比例（万分制，如1500=15.00%）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500")
    @NotNull(message = "抽成比例不能为空")
    private Integer commissionRate;

    @Schema(description = "最大同时接单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "最大同时接单数不能为空")
    private Integer maxOrderCount;

    @Schema(description = "是否推荐：0-否 1-是", example = "1")
    private Boolean isRecommend;

    @Schema(description = "技能配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "技能配置不能为空")
    @Valid
    private List<SkillItem> skills;

    @Schema(description = "审核备注", example = "审核通过，表现优秀")
    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    private String auditRemark;

    @Schema(description = "技能项")
    @Data
    public static class SkillItem {
        @Schema(description = "设备类型：1-手机 2-平板 3-PC", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "设备类型不能为空")
        private Integer deviceType;

        @Schema(description = "服务类型：1-陪玩 2-护航 3-趣味单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "服务类型不能为空")
        private Integer serviceType;

        @Schema(description = "技能等级：1-初级 2-中级 3-高级 4-资深", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        @NotNull(message = "技能等级不能为空")
        private Integer skillLevel;

        @Schema(description = "状态：0-开启 1-关闭", example = "0")
        private Integer status;
    }
}
