package cn.iocoder.yudao.module.delta.controller.app.settlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App - 打手结算分页请求 VO
 */
@Schema(description = "用户 App - 打手结算分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppDeltaWorkerSettlementPageReqVO extends PageParam {

    @Schema(description = "结算状态", example = "1")
    private Integer status;

}
