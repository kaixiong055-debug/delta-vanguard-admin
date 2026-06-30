package cn.iocoder.yudao.module.delta.controller.admin.worker.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 打手分页 Request VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 打手分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeltaWorkerPageReqVO extends PageParam {

    @Schema(description = "打手编号", example = "DW20230101120000001")
    private String workerNo;

    @Schema(description = "展示名称，模糊匹配", example = "小宋")
    private String displayName;

    @Schema(description = "真实姓名，模糊匹配", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑", example = "2")
    private Integer auditStatus;

    @Schema(description = "工作状态：0-离线 1-在线 2-忙碌 3-暂停接单", example = "1")
    private Integer workStatus;

    @Schema(description = "打手等级：1-初级 2-中级 3-高级 4-资深", example = "1")
    private Integer level;

    @Schema(description = "是否推荐：0-否 1-是", example = "1")
    private Boolean isRecommend;

    @Schema(description = "状态：0-开启 1-关闭", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
