package cn.iocoder.yudao.module.delta.controller.admin.settlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 后台 - 打手结算分页请求 VO
 */
@Schema(description = "管理后台 - 打手结算分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeltaWorkerSettlementPageReqVO extends PageParam {

    @Schema(description = "结算单号", example = "DSU202606220001")
    private String settlementNo;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "打手ID", example = "10")
    private Long workerId;

    @Schema(description = "结算状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
