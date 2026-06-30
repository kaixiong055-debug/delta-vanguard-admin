package cn.iocoder.yudao.module.delta.controller.admin.club.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 俱乐部服务范围更新 Request VO")
@Data
public class DeltaClubUpdateServiceScopeReqVO {

    @Schema(description = "俱乐部ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "俱乐部ID不能为空")
    private Long clubId;

    @Schema(description = "服务类型列表（1-陪玩 2-护航 3-趣味单）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "服务范围不能为空")
    private List<Integer> serviceTypes;

}
