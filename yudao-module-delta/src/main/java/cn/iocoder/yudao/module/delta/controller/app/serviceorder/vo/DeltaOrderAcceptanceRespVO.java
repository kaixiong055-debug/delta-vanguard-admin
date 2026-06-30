package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App/Admin - 验收记录 Response VO
 */
@Schema(description = "验收记录 Response VO")
@Data
public class DeltaOrderAcceptanceRespVO {

    @Schema(description = "验收记录ID", example = "1")
    private Long id;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "验收结果", example = "1")
    private Integer acceptanceResult;

    @Schema(description = "验收结果名称", example = "验收通过")
    private String acceptanceResultName;

    @Schema(description = "操作人类型", example = "1")
    private Integer operatorType;

    @Schema(description = "操作人类型名称", example = "客户")
    private String operatorTypeName;

    @Schema(description = "操作人名称", example = "张三")
    private String operatorName;

    @Schema(description = "备注", example = "服务已完成")
    private String remark;

    @Schema(description = "操作前状态", example = "60")
    private Integer beforeStatus;

    @Schema(description = "操作后状态", example = "80")
    private Integer afterStatus;

    @Schema(description = "验收时间")
    private LocalDateTime acceptanceTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
