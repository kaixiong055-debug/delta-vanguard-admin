package cn.iocoder.yudao.module.delta.controller.admin.club.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 俱乐部档案更新 Request VO")
@Data
public class DeltaClubUpdateReqVO {

    @Schema(description = "俱乐部ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "俱乐部ID不能为空")
    private Long id;

    @Schema(description = "俱乐部名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "先锋俱乐部")
    @NotBlank(message = "俱乐部名称不能为空")
    private String clubName;

    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @Schema(description = "联系人手机号", example = "13800138000")
    private String contactMobile;

    @Schema(description = "联系人微信", example = "wx_zhangsan")
    private String contactWechat;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "俱乐部描述")
    @Length(max = 500, message = "描述最大 500 字")
    private String description;

    @Schema(description = "平台抽成比例（万分制）", example = "500")
    private Integer platformCommissionRate;

    @Schema(description = "最大并发订单数", example = "100")
    private Integer maxConcurrentOrders;

    @Schema(description = "备注")
    @Length(max = 500, message = "备注最大 500 字")
    private String remark;

}
