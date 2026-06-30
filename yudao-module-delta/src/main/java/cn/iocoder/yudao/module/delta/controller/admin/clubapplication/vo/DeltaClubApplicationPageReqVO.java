package cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 俱乐部入驻申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaClubApplicationPageReqVO extends PageParam {

    @Schema(description = "申请编号", example = "DCA202501011200001")
    private String applicationNo;

    @Schema(description = "俱乐部名称", example = "先锋俱乐部")
    private String clubName;

    @Schema(description = "申请会员ID", example = "1024")
    private Long applicantMemberId;

    @Schema(description = "联系人手机号", example = "13800138000")
    private String contactMobile;

    @Schema(description = "申请状态", example = "0")
    private Integer applicationStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
