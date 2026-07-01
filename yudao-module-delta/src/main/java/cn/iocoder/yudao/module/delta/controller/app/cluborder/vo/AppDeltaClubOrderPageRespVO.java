package cn.iocoder.yudao.module.delta.controller.app.cluborder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 俱乐部履约订单分页 Response VO")
@Data
public class AppDeltaClubOrderPageRespVO {

    private Long listingId;
    private String listingNo;
    private Long serviceOrderId;
    private String serviceOrderNo;
    private Integer serviceType;
    private String serviceTypeName;
    private Integer deviceType;
    private String deviceTypeName;
    private Integer serviceAmount;
    private String requirementSummary;
    private Integer orderStatus;
    private String orderStatusName;
    private Long assignedWorkerId;
    private String assignedWorkerDisplayName;
    private LocalDateTime claimTime;
    private LocalDateTime acceptedAt;
    private LocalDateTime createTime;
}
