package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户 APP - 打手申请 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 打手申请 Request VO")
@Data
public class AppDeltaWorkerApplyReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Size(min = 11, max = 11, message = "手机号长度为11位")
    private String phone;

    @Schema(description = "游戏账号UID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456789")
    @NotBlank(message = "游戏账号UID不能为空")
    @Size(max = 100, message = "游戏账号UID长度不能超过100个字符")
    private String gameUid;

    @Schema(description = "设备类型：1-手机 2-平板 3-PC", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "个人介绍", example = "擅长各种游戏，经验丰富")
    @Size(max = 500, message = "个人介绍长度不能超过500个字符")
    private String introduction;

    @Schema(description = "打手经验描述", example = "5年游戏打手经验")
    @Size(max = 500, message = "经验描述长度不能超过500个字符")
    private String experience;

    @Schema(description = "证明材料图片URL列表", example = "[\"https://cdn.example.com/img1.jpg\"]")
    @Size(max = 10, message = "证明材料图片最多10张")
    private List<@URL(message = "图片URL格式不正确") String> evidenceUrls;

    @Schema(description = "审核凭证图片URL", example = "https://cdn.example.com/check.jpg")
    @URL(message = "凭证图片URL格式不正确")
    private String checkEvidenceUrl;

}
