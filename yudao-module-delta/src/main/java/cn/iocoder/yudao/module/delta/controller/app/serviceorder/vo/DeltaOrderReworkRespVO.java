package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App/Admin - 返工记录 Response VO
 */
@Schema(description = "返工记录 Response VO")
@Data
public class DeltaOrderReworkRespVO {

    @Schema(description = "返工记录ID", example = "1")
    private Long id;

    @Schema(description = "服务订单ID", example = "100")
    private Long serviceOrderId;

    @Schema(description = "返工序号", example = "1")
    private Integer reworkNo;

    @Schema(description = "返工原因", example = "任务未完成")
    private String reason;

    @Schema(description = "操作人类型", example = "1")
    private Integer operatorType;

    @Schema(description = "操作人类型名称", example = "客户")
    private String operatorTypeName;

    @Schema(description = "操作人名称", example = "张三")
    private String operatorName;

    @Schema(description = "操作前状态", example = "60")
    private Integer beforeStatus;

    @Schema(description = "操作后状态", example = "50")
    private Integer afterStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
