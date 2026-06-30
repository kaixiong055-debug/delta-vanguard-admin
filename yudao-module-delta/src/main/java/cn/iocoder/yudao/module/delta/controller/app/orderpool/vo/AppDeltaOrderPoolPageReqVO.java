package cn.iocoder.yudao.module.delta.controller.app.orderpool.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - 订单池分页 Req VO
 */
@Schema(description = "用户 App - 订单池分页 Request VO")
@Data
public class AppDeltaOrderPoolPageReqVO extends PageParam {

    @Schema(description = "设备类型：1-手机 2-平板 3-PC", example = "1")
    private Integer deviceType;

    @Schema(description = "服务类型：1-陪玩 2-护航 3-趣味单", example = "1")
    private Integer serviceType;

}
