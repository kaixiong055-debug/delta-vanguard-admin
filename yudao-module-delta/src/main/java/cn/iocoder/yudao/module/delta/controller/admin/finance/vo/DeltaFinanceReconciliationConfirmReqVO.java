package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 确认对账请求 VO
 */
@Schema(description = "管理后台 - 确认对账请求")
@Data
public class DeltaFinanceReconciliationConfirmReqVO {

    @Schema(description = "对账ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对账ID不能为空")
    private Long id;

    @Schema(description = "确认备注")
    @Length(max = 500, message = "确认备注最长500字")
    private String remark;

}
