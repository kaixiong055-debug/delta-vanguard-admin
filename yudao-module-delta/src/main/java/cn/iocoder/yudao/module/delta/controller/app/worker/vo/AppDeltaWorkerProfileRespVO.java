package cn.iocoder.yudao.module.delta.controller.app.worker.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户 APP - 打手资料 Response VO
 *
 * @author Delta-Vanguard
 */
@Schema(description = "用户 App - 打手资料 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppDeltaWorkerProfileRespVO {

    @Schema(description = "打手ID", example = "1")
    private Long workerId;

    @Schema(description = "打手编号", example = "DW20230101120000001")
    private String workerNo;

    @Schema(description = "展示名称", example = "小宋")
    private String displayName;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑", example = "2")
    private Integer auditStatus;

    @Schema(description = "工作状态：0-离线 1-在线 2-忙碌 3-暂停接单", example = "0")
    private Integer workStatus;

    @Schema(description = "打手等级：1-初级 2-中级 3-高级 4-资深", example = "1")
    private Integer level;

    @Schema(description = "评分（万分制）", example = "49500")
    private Integer score;

    @Schema(description = "抽成比例（万分制，如1500=15.00%）", example = "1500")
    private Integer commissionRate;

    @Schema(description = "最大同时接单数", example = "2")
    private Integer maxOrderCount;

    @Schema(description = "当前进行中订单数", example = "0")
    private Integer currentOrderCount;

    @Schema(description = "历史完成订单数", example = "100")
    private Integer completedOrderCount;

    @Schema(description = "取消订单数", example = "5")
    private Integer cancelOrderCount;

    @Schema(description = "是否启用（状态：0-开启 1-关闭）", example = "0")
    private Integer enabledStatus;

    @Schema(description = "技能列表")
    private List<SkillInfo> skills;

    @Schema(description = "打手技能信息")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        @Schema(description = "技能ID", example = "1")
        private Long id;

        @Schema(description = "设备类型：1-手机 2-平板 3-PC", example = "1")
        private Integer deviceType;

        @Schema(description = "服务类型：1-陪玩 2-护航 3-趣味单", example = "1")
        private Integer serviceType;

        @Schema(description = "技能等级：1-初级 2-中级 3-高级 4-资深", example = "2")
        private Integer skillLevel;

        @Schema(description = "状态：0-开启 1-关闭", example = "0")
        private Integer status;
    }
}
