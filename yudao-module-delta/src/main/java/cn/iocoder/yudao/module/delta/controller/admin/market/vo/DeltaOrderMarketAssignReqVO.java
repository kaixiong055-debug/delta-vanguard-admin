package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 平台指定俱乐部 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 平台指定俱乐部接单 Request VO")
@Data
public class DeltaOrderMarketAssignReqVO {

    @Schema(description = "挂牌ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "挂牌ID不能为空")
    private Long id;

    @Schema(description = "俱乐部档案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "俱乐部ID不能为空")
    private Long clubId;

    @Schema(description = "备注", example = "平台指定")
    private String remark;
}
