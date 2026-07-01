package cn.iocoder.yudao.module.delta.controller.app.cluborder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 俱乐部可分派打手 Response VO")
@Data
public class AppDeltaClubOrderWorkerRespVO {

    private Long id;
    private String workerNo;
    private String displayName;
    private String avatar;
    private Integer level;
    private Integer score;
    private Integer workStatus;
    private String workStatusName;
    private Integer currentOrderCount;
    private Integer maxOrderCount;
}
