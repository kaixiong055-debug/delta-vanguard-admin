package cn.iocoder.yudao.module.delta.controller.admin.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 俱乐部分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaClubPageReqVO extends PageParam {

    @Schema(description = "俱乐部编码", example = "DC202501011200001")
    private String clubCode;

    @Schema(description = "俱乐部名称", example = "先锋俱乐部")
    private String clubName;

    @Schema(description = "所有者会员ID", example = "1024")
    private Long ownerMemberId;

    @Schema(description = "经营状态：0-停用 1-启用", example = "1")
    private Integer businessStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
