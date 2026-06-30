package cn.iocoder.yudao.module.delta.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 发布到市场 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 发布到订单市场 Request VO")
@Data
public class DeltaOrderMarketPublishReqVO {

    @Schema(description = "服务订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "服务订单ID不能为空")
    private Long serviceOrderId;

    @Schema(description = "过期时间", example = "2026-07-01 12:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "需求摘要（脱敏后）", example = "需要陪玩服务，王者荣耀钻石段位")
    private String requirementSummary;

    @Schema(description = "备注", example = "平台挂牌")
    private String remark;
}
