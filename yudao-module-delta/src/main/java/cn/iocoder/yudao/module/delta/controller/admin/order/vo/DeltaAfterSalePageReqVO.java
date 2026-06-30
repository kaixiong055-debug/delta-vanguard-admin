package cn.iocoder.yudao.module.delta.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * Admin - 售后案件分页 Request VO
 */
@Schema(description = "管理后台 - 售后案件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaAfterSalePageReqVO extends PageParam {

    @Schema(description = "售后单号", example = "DAS202401010000001")
    private String afterSaleNo;

    @Schema(description = "服务订单ID", example = "1")
    private Long serviceOrderId;

    @Schema(description = "买家用户ID", example = "1")
    private Long buyerUserId;

    @Schema(description = "打手ID", example = "1")
    private Long workerId;

    @Schema(description = "售后状态：0-待处理 1-已受理 2-已驳回 3-已仲裁 4-已关闭", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
