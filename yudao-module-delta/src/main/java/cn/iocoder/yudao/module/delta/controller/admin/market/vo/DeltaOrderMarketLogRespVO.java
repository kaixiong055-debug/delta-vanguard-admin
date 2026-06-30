package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 市场操作日志 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 市场操作日志 Response VO")
@Data
public class DeltaOrderMarketLogRespVO {

    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "挂牌ID", example = "1")
    private Long listingId;

    @Schema(description = "服务订单ID", example = "1001")
    private Long serviceOrderId;

    @Schema(description = "操作类型", example = "PUBLISH")
    private String operationType;

    @Schema(description = "操作类型名称", example = "发布挂牌")
    private String operationTypeName;

    @Schema(description = "操作方类型", example = "PLATFORM")
    private String operatorType;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作俱乐部ID", example = "10")
    private Long clubId;

    @Schema(description = "操作俱乐部租户ID", example = "100")
    private Long clubTenantId;

    @Schema(description = "操作前状态", example = "0")
    private Integer beforeStatus;

    @Schema(description = "操作后状态", example = "1")
    private Integer afterStatus;

    @Schema(description = "是否成功", example = "1")
    private Integer success;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
