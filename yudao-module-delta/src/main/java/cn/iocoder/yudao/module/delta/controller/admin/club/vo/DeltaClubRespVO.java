package cn.iocoder.yudao.module.delta.controller.admin.club.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 俱乐部 Response VO")
@Data
public class DeltaClubRespVO {

    @Schema(description = "档案ID", example = "1")
    private Long id;

    @Schema(description = "租户ID", example = "100")
    private Long tenantId;

    @Schema(description = "俱乐部编码", example = "DC202501011200001")
    private String clubCode;

    @Schema(description = "俱乐部名称", example = "先锋俱乐部")
    private String clubName;

    @Schema(description = "所有者会员ID", example = "1024")
    private Long ownerMemberId;

    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @Schema(description = "联系人手机号", example = "13800138000")
    private String contactMobile;

    @Schema(description = "联系人微信", example = "wx_zhangsan")
    private String contactWechat;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "俱乐部描述")
    private String description;

    @Schema(description = "经营状态：0-停用 1-启用", example = "1")
    private Integer businessStatus;

    @Schema(description = "经营状态名称", example = "启用")
    private String businessStatusName;

    @Schema(description = "平台抽成比例（万分制）", example = "500")
    private Integer platformCommissionRate;

    @Schema(description = "最大并发订单数", example = "100")
    private Integer maxConcurrentOrders;

    @Schema(description = "入驻申请ID", example = "1")
    private Long applicationId;

    @Schema(description = "服务范围")
    private List<ServiceScopeItem> serviceScopes;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Data
    @Schema(description = "服务范围项")
    public static class ServiceScopeItem {

        @Schema(description = "ID", example = "1")
        private Long id;

        @Schema(description = "服务类型", example = "1")
        private Integer serviceType;

        @Schema(description = "服务类型名称", example = "陪玩")
        private String serviceTypeName;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        @Schema(description = "备注")
        private String remark;
    }

}
