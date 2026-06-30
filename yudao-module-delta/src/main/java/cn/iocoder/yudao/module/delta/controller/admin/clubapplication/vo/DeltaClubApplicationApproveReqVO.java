package cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 俱乐部入驻申请审核通过 Request VO")
@Data
public class DeltaClubApplicationApproveReqVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "已有租户ID（需提前在系统管理中创建）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "平台抽成比例（万分制，如 500 = 5.00%）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @NotNull(message = "平台抽成比例不能为空")
    private Integer platformCommissionRate;

    @Schema(description = "最大并发订单数", example = "100")
    private Integer maxConcurrentOrders;

    @Schema(description = "服务类型列表（1-陪玩 2-护航 3-趣味单）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "服务范围不能为空")
    private List<Integer> serviceTypes;

    @Schema(description = "审核备注", example = "资质齐全，准予入驻")
    private String remark;

}
