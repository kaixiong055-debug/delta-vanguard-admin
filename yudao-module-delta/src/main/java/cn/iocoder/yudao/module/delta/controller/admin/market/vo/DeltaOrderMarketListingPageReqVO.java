package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 平台挂牌分页 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 订单市场挂牌分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaOrderMarketListingPageReqVO extends PageParam {

    @Schema(description = "挂牌编号", example = "DML202506301200001")
    private String listingNo;

    @Schema(description = "服务订单号", example = "DSO202506301200001")
    private String serviceOrderNo;

    @Schema(description = "服务类型：1-陪玩 2-护航 3-趣味单", example = "1")
    private Integer serviceType;

    @Schema(description = "挂牌状态：0-可抢 1-已被接单 2-已撤回 3-已过期 4-已关闭", example = "0")
    private Integer listingStatus;

    @Schema(description = "接单俱乐部ID", example = "1")
    private Long claimedClubId;

    @Schema(description = "接单俱乐部租户ID", example = "100")
    private Long claimedClubTenantId;

    @Schema(description = "发布时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] publishTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
