package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - 关闭售后 Request VO
 */
@Schema(description = "管理后台 - 关闭售后 Request VO")
@Data
public class DeltaAfterSaleCloseReqVO {

    @Schema(description = "售后案件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "售后案件ID不能为空")
    private Long id;

    @Schema(description = "备注", example = "案件已处理完成")
    private String remark;

}
