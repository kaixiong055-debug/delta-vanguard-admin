package cn.iocoder.yudao.module.delta.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 订单市场安全 Response VO")
@Data
public class AppDeltaOrderMarketRespVO {

    private Long id;
    private String listingNo;
    private Integer serviceType;
    private String serviceTypeName;
    private Integer serviceAmount;
    private String requirementSummary;
    private Integer listingStatus;
    private String listingStatusName;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private LocalDateTime claimTime;
}
