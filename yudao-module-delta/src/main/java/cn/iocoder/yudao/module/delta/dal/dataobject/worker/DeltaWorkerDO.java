package cn.iocoder.yudao.module.delta.dal.dataobject.worker;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 打手资料 DO
 *
 * @author Delta-Vanguard
 */
@TableName(value = "delta_worker", autoResultMap = true)
@KeySequence("delta_worker_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeltaWorkerDO extends TenantBaseDO {

    @TableId
    private Long id;
    /**
     * 会员用户ID（关联 member_user.id）
     */
    private Long userId;
    /**
     * 打手编号
     */
    private String workerNo;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 展示名称
     */
    private String displayName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑
     */
    private Integer auditStatus;
    /**
     * 工作状态：0-离线 1-在线 2-忙碌 3-暂停接单
     */
    private Integer workStatus;
    /**
     * 打手等级：1-初级 2-中级 3-高级 4-资深
     */
    private Integer level;
    /**
     * 评分（万分制，如49500表示4.95分）
     */
    private Integer score;
    /**
     * 抽成比例（万分制，如1500表示15.00%）
     */
    private Integer commissionRate;
    /**
     * 最大同时接单数
     */
    private Integer maxOrderCount;
    /**
     * 当前进行中订单数
     */
    private Integer currentOrderCount;
    /**
     * 历史完成订单数
     */
    private Integer completedOrderCount;
    /**
     * 取消订单数
     */
    private Integer cancelOrderCount;
    /**
     * 是否推荐：0-否 1-是
     */
    private Boolean isRecommend;
    /**
     * 状态：0-开启 1-关闭（对应 CommonStatusEnum）
     */
    private Integer status;
    /**
     * 审核备注
     */
    private String auditRemark;
    /**
     * 审核通过时间
     */
    private LocalDateTime approvedAt;

}
