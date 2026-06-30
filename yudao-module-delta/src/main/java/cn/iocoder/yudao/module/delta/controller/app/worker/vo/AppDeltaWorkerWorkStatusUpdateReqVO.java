package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户 APP - 修改工作状态 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 修改工作状态 Request VO")
@Data
public class AppDeltaWorkerWorkStatusUpdateReqVO {

    @Schema(description = "工作状态：0-离线 1-在线 3-暂停接单（不允许主动设为忙碌(2)）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工作状态不能为空")
    private Integer workStatus;

}
