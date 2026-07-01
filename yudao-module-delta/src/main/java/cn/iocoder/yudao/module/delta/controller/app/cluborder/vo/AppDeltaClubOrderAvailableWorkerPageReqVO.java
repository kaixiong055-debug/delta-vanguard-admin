package cn.iocoder.yudao.module.delta.controller.app.cluborder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 俱乐部可分派打手分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppDeltaClubOrderAvailableWorkerPageReqVO extends PageParam {

    @NotNull(message = "挂牌 ID 不能为空")
    private Long listingId;
}
