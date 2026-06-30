package cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Schema(description = "用户 APP - 俱乐部入驻申请提交 Request VO")
@Data
public class AppDeltaClubApplicationSubmitReqVO {

    @Schema(description = "俱乐部名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "先锋俱乐部")
    @NotBlank(message = "俱乐部名称不能为空")
    @Length(max = 100, message = "俱乐部名称最大 100 字")
    private String clubName;

    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "联系人姓名不能为空")
    @Length(max = 50, message = "联系人姓名最大 50 字")
    private String contactName;

    @Schema(description = "联系人手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系人手机号不能为空")
    @Length(max = 20, message = "手机号最大 20 位")
    private String contactMobile;

    @Schema(description = "联系人微信", example = "wx_zhangsan")
    @Length(max = 50, message = "微信号最大 50 字")
    private String contactWechat;

    @Schema(description = "俱乐部描述", example = "专业的游戏陪玩/护航俱乐部")
    @Length(max = 2000, message = "描述最大 2000 字")
    private String description;

    @Schema(description = "Logo URL")
    @Length(max = 500, message = "Logo URL 最大 500 字")
    private String logoUrl;

    @Schema(description = "资质凭证图片URL列表 (JSON数组)", example = "[\"https://cdn.example.com/qual1.jpg\"]")
    @Length(max = 3000, message = "资质凭证URL最大 3000 字")
    private String qualificationUrls;

}
