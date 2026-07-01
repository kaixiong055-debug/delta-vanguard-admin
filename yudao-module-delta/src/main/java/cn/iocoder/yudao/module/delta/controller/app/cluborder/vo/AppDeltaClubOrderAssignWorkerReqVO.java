package cn.iocoder.yudao.module.delta.controller.app.cluborder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "用户 App - 俱乐部分派打手 Request VO")
@Data
public class AppDeltaClubOrderAssignWorkerReqVO {

    @NotNull(message = "挂牌 ID 不能为空")
    private Long listingId;

    @NotNull(message = "打手 ID 不能为空")
    private Long workerId;

    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
