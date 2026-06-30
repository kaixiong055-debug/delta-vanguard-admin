package cn.iocoder.yudao.module.delta.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商品服务配置 Save VO（创建/更新共用）
 */
@Data
public class DeltaProductServiceConfigSaveReqVO {

    @Schema(description = "配置ID（更新时必填）", example = "1")
    private Long id;

    @Schema(description = "商品SPU ID", example = "1001")
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    @Schema(description = "商品SKU ID", example = "2001")
    private Long skuId;

    @Schema(description = "服务类型 1-陪玩 2-护航 3-趣味单", example = "1")
    @NotNull(message = "服务类型不能为空")
    private Integer serviceType;

    @Schema(description = "设备类型 1-手机 2-平板 3-PC", example = "1")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "所需打手等级 1-初级 2-中级 3-高级 4-资深", example = "1")
    private Integer requiredWorkerLevel;

    @Schema(description = "是否允许客户指定打手")
    private Boolean allowDesignatedWorker;

    @Schema(description = "是否允许进入接单大厅")
    private Boolean allowPublicClaim;

    @Schema(description = "默认派单模式 1-客户指定 2-客服派单 3-接单大厅", example = "2")
    private Integer defaultDispatchMode;

    @Schema(description = "最大服务时长(小时)", example = "8")
    private Integer maxServiceHours;

    @Schema(description = "佣金比例(万分制)", example = "1500")
    private Integer commissionRate;

    @Schema(description = "是否启用")
    private Boolean enabled;

}
