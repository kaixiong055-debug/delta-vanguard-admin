package cn.iocoder.yudao.module.delta.controller.admin.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 管理后台 - 修改打手资料 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 修改打手资料 Request VO")
@Data
public class DeltaWorkerUpdateReqVO {

    @Schema(description = "打手ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "打手ID不能为空")
    private Long id;

    @Schema(description = "展示名称", example = "小宋")
    @Size(max = 50, message = "展示名称长度不能超过50个字符")
    private String displayName;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "打手等级：1-初级 2-中级 3-高级 4-资深", example = "2")
    private Integer level;

    @Schema(description = "抽成比例（万分制，如1500=15.00%）", example = "1500")
    private Integer commissionRate;

    @Schema(description = "最大同时接单数", example = "3")
    private Integer maxOrderCount;

    @Schema(description = "是否推荐：0-否 1-是", example = "1")
    private Boolean isRecommend;

    @Schema(description = "审核备注", example = "该打手表现优秀")
    private String auditRemark;

}
