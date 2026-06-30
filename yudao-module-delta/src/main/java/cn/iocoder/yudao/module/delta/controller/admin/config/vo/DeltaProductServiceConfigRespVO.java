package cn.iocoder.yudao.module.delta.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品服务配置响应 VO
 */
@Data
public class DeltaProductServiceConfigRespVO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "商品SPU ID", example = "1001")
    private Long spuId;

    @Schema(description = "商品SKU ID", example = "2001")
    private Long skuId;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "所需打手等级", example = "1")
    private Integer requiredWorkerLevel;

    @Schema(description = "是否允许指定打手")
    private Boolean allowDesignatedWorker;

    @Schema(description = "是否允许接单大厅")
    private Boolean allowPublicClaim;

    @Schema(description = "默认派单模式", example = "2")
    private Integer defaultDispatchMode;

    @Schema(description = "最大服务时长(小时)", example = "8")
    private Integer maxServiceHours;

    @Schema(description = "佣金比例(万分制)", example = "1500")
    private Integer commissionRate;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
