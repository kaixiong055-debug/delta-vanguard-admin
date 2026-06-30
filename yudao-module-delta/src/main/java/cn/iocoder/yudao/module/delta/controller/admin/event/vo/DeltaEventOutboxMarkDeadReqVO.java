package cn.iocoder.yudao.module.delta.controller.admin.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Admin - Outbox 事件标记死亡请求 VO
 */
@Schema(description = "管理后台 - Outbox 事件标记死亡 Request VO")
@Data
public class DeltaEventOutboxMarkDeadReqVO {

    @Schema(description = "事件ID", required = true, example = "1")
    @NotNull(message = "事件ID不能为空")
    private Long id;
}
