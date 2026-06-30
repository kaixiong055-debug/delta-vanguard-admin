package cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Admin 服务履约订单分页查询 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeltaServiceOrderPageReqVO extends PageParam {

    @Schema(description = "服务订单号", example = "DSO202306011200001")
    private String serviceOrderNo;

    @Schema(description = "商城订单号", example = "202305310001")
    private String tradeOrderNo;

    @Schema(description = "会员ID", example = "1001")
    private Long buyerUserId;

    @Schema(description = "SPU ID", example = "1001")
    private Long spuId;

    @Schema(description = "SKU ID", example = "2001")
    private Long skuId;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "履约状态", example = "10")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
