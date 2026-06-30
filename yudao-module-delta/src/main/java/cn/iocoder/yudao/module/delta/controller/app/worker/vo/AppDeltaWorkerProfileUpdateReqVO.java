package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 用户 APP - 修改打手资料 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 修改打手资料 Request VO")
@Data
public class AppDeltaWorkerProfileUpdateReqVO {

    @Schema(description = "展示名称", example = "小宋")
    private String displayName;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar.jpg")
    @URL(message = "头像URL格式不正确")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

}
