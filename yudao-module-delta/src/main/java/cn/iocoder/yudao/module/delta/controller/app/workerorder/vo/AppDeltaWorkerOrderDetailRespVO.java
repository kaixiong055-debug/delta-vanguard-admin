package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App - 打手订单详情 Resp VO
 */
@Schema(description = "用户 App - 打手订单详情 Response VO")
@Data
public class AppDeltaWorkerOrderDetailRespVO {

    @Schema(description = "服务单ID", example = "1")
    private Long id;

    @Schema(description = "服务订单号", example = "DSO202401010000011")
    private String serviceOrderNo;

    @Schema(description = "商城订单号", example = "T202401010000011")
    private String tradeOrderNo;

    @Schema(description = "商品名称", example = "王者荣耀陪玩1小时")
    private String productName;

    @Schema(description = "SKU属性", example = "[]")
    private String skuName;

    @Schema(description = "商品图片")
    private String productPicUrl;

    @Schema(description = "购买数量", example = "1")
    private Integer count;

    @Schema(description = "服务金额（分）", example = "5000")
    private Integer serviceAmount;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "服务类型名称", example = "陪玩")
    private String serviceTypeName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "手机")
    private String deviceTypeName;

    @Schema(description = "派单方式", example = "3")
    private Integer dispatchMode;

    @Schema(description = "状态", example = "40")
    private Integer status;

    @Schema(description = "状态名称", example = "已接单待开始")
    private String statusName;

    @Schema(description = "客户备注")
    private String customerRemark;

    @Schema(description = "分配时间")
    private LocalDateTime assignTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
