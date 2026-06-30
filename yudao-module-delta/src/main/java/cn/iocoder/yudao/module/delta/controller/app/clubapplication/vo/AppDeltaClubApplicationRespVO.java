package cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 俱乐部入驻申请 Response VO")
@Data
public class AppDeltaClubApplicationRespVO {

    @Schema(description = "申请ID", example = "1")
    private Long id;

    @Schema(description = "申请编号", example = "DCA202501011200001")
    private String applicationNo;

    @Schema(description = "俱乐部名称", example = "先锋俱乐部")
    private String clubName;

    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    @Schema(description = "联系人手机号", example = "13800138000")
    private String contactMobile;

    @Schema(description = "联系人微信", example = "wx_zhangsan")
    private String contactWechat;

    @Schema(description = "俱乐部描述")
    private String description;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "资质凭证图片URL列表")
    private String qualificationUrls;

    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销", example = "0")
    private Integer applicationStatus;

    @Schema(description = "申请状态名称", example = "待审核")
    private String applicationStatusName;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

}
