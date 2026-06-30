package cn.iocoder.yudao.module.delta.controller.admin.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - 修改打手状态 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 修改打手状态 Request VO")
@Data
public class DeltaWorkerStatusUpdateReqVO {

    @Schema(description = "打手ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打手ID不能为空")
    private Long id;

    @Schema(description = "状态：0-开启 1-关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "操作原因/备注", example = "暂时停用")
    private String reason;

}
