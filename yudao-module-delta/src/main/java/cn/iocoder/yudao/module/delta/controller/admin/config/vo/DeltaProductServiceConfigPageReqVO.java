package cn.iocoder.yudao.module.delta.controller.admin.config.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品服务配置分页查询 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeltaProductServiceConfigPageReqVO extends PageParam {

    @Schema(description = "商品SPU ID", example = "1001")
    private Long spuId;

    @Schema(description = "商品SKU ID", example = "2001")
    private Long skuId;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "是否启用")
    private Boolean enabled;

}
