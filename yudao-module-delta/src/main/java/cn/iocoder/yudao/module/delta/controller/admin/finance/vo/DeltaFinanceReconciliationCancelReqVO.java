package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 取消对账请求 VO
 */
@Schema(description = "管理后台 - 取消对账请求")
@Data
public class DeltaFinanceReconciliationCancelReqVO {

    @Schema(description = "对账ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对账ID不能为空")
    private Long id;

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "取消原因不能为空")
    @Length(max = 500, message = "取消原因最长500字")
    private String reason;

}
