package cn.iocoder.yudao.module.delta.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商品服务配置状态更新 VO
 */
@Data
public class DeltaProductServiceConfigUpdateStatusReqVO {

    @Schema(description = "配置ID", example = "1")
    @NotNull(message = "配置ID不能为空")
    private Long id;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

}
