package cn.iocoder.yudao.module.delta.controller.admin.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 打手信息 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "管理后台 - 打手信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerRespVO {

    @Schema(description = "打手ID", example = "1")
    private Long id;

    @Schema(description = "会员用户ID", example = "100")
    private Long userId;

    @Schema(description = "会员昵称", example = "老板小王")
    private String memberNickname;

    @Schema(description = "会员头像", example = "https://cdn.example.com/avatar.jpg")
    private String memberAvatar;

    @Schema(description = "打手编号", example = "DW20230101120000001")
    private String workerNo;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "展示名称", example = "小宋")
    private String displayName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "审核状态", example = "2")
    private Integer auditStatus;

    @Schema(description = "工作状态", example = "1")
    private Integer workStatus;

    @Schema(description = "打手等级", example = "1")
    private Integer level;

    @Schema(description = "评分（万分制）", example = "49500")
    private Integer score;

    @Schema(description = "抽成比例（万分制）", example = "1500")
    private Integer commissionRate;

    @Schema(description = "最大同时接单数", example = "2")
    private Integer maxOrderCount;

    @Schema(description = "当前进行中订单数", example = "1")
    private Integer currentOrderCount;

    @Schema(description = "历史完成订单数", example = "100")
    private Integer completedOrderCount;

    @Schema(description = "取消订单数", example = "5")
    private Integer cancelOrderCount;

    @Schema(description = "是否推荐：0-否 1-是", example = "1")
    private Boolean isRecommend;

    @Schema(description = "状态：0-开启 1-关闭", example = "0")
    private Integer status;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

    @Schema(description = "审核通过时间", example = "2023-01-01 12:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "技能摘要（逗号分隔）", example = "手机陪玩(中级), 平板护航(高级)")
    private String skillSummary;

    @Schema(description = "技能列表")
    private List<SkillInfo> skills;

    @Schema(description = "创建时间", example = "2023-01-01 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "打手技能信息")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        @Schema(description = "技能ID", example = "1")
        private Long id;

        @Schema(description = "设备类型", example = "1")
        private Integer deviceType;

        @Schema(description = "服务类型", example = "1")
        private Integer serviceType;

        @Schema(description = "技能等级", example = "2")
        private Integer skillLevel;

        @Schema(description = "状态：0-开启 1-关闭", example = "0")
        private Integer status;
    }

}
