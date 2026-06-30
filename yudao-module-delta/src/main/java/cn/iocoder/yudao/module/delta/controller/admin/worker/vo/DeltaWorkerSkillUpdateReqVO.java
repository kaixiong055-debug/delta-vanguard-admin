package cn.iocoder.yudao.module.delta.controller.admin.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理后台 - 打手技能更新 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 打手技能更新 Request VO")
@Data
public class DeltaWorkerSkillUpdateReqVO {

    @Schema(description = "打手ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打手ID不能为空")
    private Long workerId;

    @Schema(description = "技能列表（空列表表示清空所有技能）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "技能列表不能为空")
    @Valid
    private List<SkillItem> skills;

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
