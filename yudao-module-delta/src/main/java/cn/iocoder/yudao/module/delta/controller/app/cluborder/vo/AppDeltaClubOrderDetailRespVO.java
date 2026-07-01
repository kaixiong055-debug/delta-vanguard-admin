package cn.iocoder.yudao.module.delta.controller.app.cluborder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 俱乐部履约订单详情 Response VO")
@Data
public class AppDeltaClubOrderDetailRespVO {

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
    private String productName;
    private String skuName;
    private String productPicUrl;
    private Integer count;
    private String customerRemark;
    private Integer orderStatus;
    private String orderStatusName;
    private Long assignedWorkerId;
    private String assignedWorkerNo;
    private String assignedWorkerDisplayName;
    private String assignedWorkerAvatar;
    private LocalDateTime claimTime;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createTime;
}
