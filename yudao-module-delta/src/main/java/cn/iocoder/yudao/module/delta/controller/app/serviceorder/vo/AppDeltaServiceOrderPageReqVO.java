package cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * App 服务履约订单分页查询 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppDeltaServiceOrderPageReqVO extends PageParam {

    @Schema(description = "用户ID（服务端从登录上下文获取，不从前端接收）", hidden = true)
    private Long userId;

    @Schema(description = "履约状态", example = "10")
    private Integer status;

    @Schema(description = "服务类型", example = "1")
    private Integer serviceType;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
