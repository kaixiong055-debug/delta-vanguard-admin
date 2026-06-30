package cn.iocoder.yudao.module.delta.controller.app.workerorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - 打手订单分页 Req VO
 */
@Schema(description = "用户 App - 打手订单分页 Request VO")
@Data
public class AppDeltaWorkerOrderPageReqVO extends PageParam {

    @Schema(description = "状态", example = "40")
    private Integer status;

}
