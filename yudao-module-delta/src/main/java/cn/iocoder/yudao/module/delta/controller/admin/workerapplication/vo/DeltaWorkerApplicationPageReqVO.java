package cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 打手申请分页 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 打手申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaWorkerApplicationPageReqVO extends PageParam {

    @Schema(description = "会员用户ID", example = "100")
    private Long userId;

    @Schema(description = "真实姓名，模糊匹配", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "设备类型：1-手机 2-平板 3-PC", example = "1")
    private Integer deviceType;

    @Schema(description = "申请状态：0-未申请 1-审核中 2-审核通过 3-审核驳回", example = "1")
    private Integer applicationStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
