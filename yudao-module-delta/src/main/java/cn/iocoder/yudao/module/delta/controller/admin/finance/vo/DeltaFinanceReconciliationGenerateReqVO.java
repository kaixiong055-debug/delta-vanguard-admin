package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 生成对账请求 VO
 */
@Schema(description = "管理后台 - 生成对账请求")
@Data
public class DeltaFinanceReconciliationGenerateReqVO {

    @Schema(description = "对账日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对账日期不能为空")
    private LocalDate reconciliationDate;

}
