package cn.iocoder.yudao.module.delta.controller.admin.finance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对账记录分页请求 VO
 */
@Schema(description = "管理后台 - 对账记录分页请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeltaFinanceReconciliationPageReqVO extends PageParam {

    @Schema(description = "对账单号")
    private String reconciliationNo;

    @Schema(description = "对账日期")
    private LocalDate reconciliationDate;

    @Schema(description = "状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
