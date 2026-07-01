package cn.iocoder.yudao.module.delta.controller.app.club.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Schema(description = "用户 App - 俱乐部身份 Response VO")
@Data
public class AppDeltaClubIdentityRespVO {

    private Boolean hasApplication;
    private Integer applicationStatus;
    private String applicationStatusName;
    private String rejectReason;

    private Boolean isClubOwner;
    private Boolean canUseOrderMarket;

    private Long clubId;
    private String clubCode;
    private String clubName;
    private String logoUrl;
    private String description;
    private Integer businessStatus;
    private String businessStatusName;
    private Integer platformCommissionRate;
    private Integer maxConcurrentOrders;

    private List<ServiceScopeItem> serviceScopes = Collections.emptyList();

    @Data
    public static class ServiceScopeItem {
        private Integer serviceType;
        private String serviceTypeName;
        private Boolean enabled;
    }
}
