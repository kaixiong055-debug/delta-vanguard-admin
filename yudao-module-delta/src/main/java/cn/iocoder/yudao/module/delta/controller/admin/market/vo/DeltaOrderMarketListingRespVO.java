package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 挂牌 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 订单市场挂牌 Response VO")
@Data
public class DeltaOrderMarketListingRespVO {

    @Schema(description = "挂牌ID", example = "1")
    private Long id;

    @Schema(description = "挂牌编号", example = "DML202506301200001")
    private String listingNo;

    @Schema(description = "服务订单ID", example = "1001")
    private Long serviceOrderId;

    @Schema(description = "服务订单号", example = "DSO202506301200001")
    private String serviceOrderNo;

    @Schema(description = "订单来源租户ID", example = "100")
    private Long sourceTenantId;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "服务类型名称", example = "陪玩")
    private String serviceTypeName;

    @Schema(description = "服务金额（分）", example = "5000")
    private Integer serviceAmount;

    @Schema(description = "需求摘要", example = "需要陪玩服务，王者荣耀钻石段位")
    private String requirementSummary;

    @Schema(description = "挂牌状态：0-可抢 1-已被接单 2-已撤回 3-已过期 4-已关闭", example = "0")
    private Integer listingStatus;

    @Schema(description = "挂牌状态名称", example = "可抢")
    private String listingStatusName;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "接单俱乐部档案ID")
    private Long claimedClubId;

    @Schema(description = "接单俱乐部租户ID")
    private Long claimedClubTenantId;

    @Schema(description = "接单时间")
    private LocalDateTime claimTime;

    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "撤回原因")
    private String withdrawReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
