package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 服务履约订单详情响应 VO（包含快照信息）
 */
@Data
public class AppDeltaServiceOrderDetailRespVO {

    @Schema(description = "服务订单ID", example = "1")
    private Long id;

    @Schema(description = "服务订单号", example = "DSO202306011200001")
    private String serviceOrderNo;

    @Schema(description = "商城订单ID", example = "100")
    private Long tradeOrderId;

    @Schema(description = "商城订单号", example = "202305310001")
    private String tradeOrderNo;

    @Schema(description = "商城订单项ID", example = "200")
    private Long tradeOrderItemId;

    @Schema(description = "SPU ID", example = "1001")
    private Long spuId;

    @Schema(description = "SKU ID", example = "2001")
    private Long skuId;

    @Schema(description = "商品名称", example = "王者荣耀陪玩")
    private String productName;

    @Schema(description = "SKU属性", example = "1小时体验")
    private String skuName;

    @Schema(description = "商品图片")
    private String productPicUrl;

    @Schema(description = "购买数量", example = "1")
    private Integer count;

    @Schema(description = "实付金额(分)", example = "5000")
    private Integer serviceAmount;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "服务名称快照")
    private String serviceName;

    @Schema(description = "服务配置快照")
    private String serviceConfigSnapshot;

    @Schema(description = "履约状态", example = "10")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
