package cn.iocoder.yudao.module.delta.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Delta 错误码枚举
 * <p>
 * 错误码段：1-020-xxx-xxx（Delta 先锋俱乐部专用）
 * 系统编号：1
 * 模块编号：020（Delta模块）
 * <p>
 * 已占用错误码段：
 * 1-001(Framework/Infra), 1-002(System),
 * 1-004(Member), 1-007(Pay), 1-008(Product),
 * 1-011(Trade), 1-013(Promotion)
 * <p>
 * 采用 1-020 作为 Delta 模块专用段号，整体占用范围 1-020-000-000 ~ 1-020-999-999，
 * 与已有模块无冲突。
 *
 * @author Delta-Vanguard
 */
public interface ErrorCodeConstants {

    // ========== 打手 1-020-001-xxx ==========
    ErrorCode WORKER_NOT_EXISTS = new ErrorCode(1_020_001_000, "打手不存在");
    ErrorCode WORKER_NOT_APPROVED = new ErrorCode(1_020_001_001, "打手未通过审核");
    ErrorCode WORKER_DISABLED = new ErrorCode(1_020_001_002, "打手已被禁用");
    ErrorCode WORKER_ALREADY_EXISTS = new ErrorCode(1_020_001_003, "该用户已是打手");

    // ========== 打手申请 1-020-002-xxx ==========
    ErrorCode WORKER_APPLICATION_NOT_EXISTS = new ErrorCode(1_020_002_000, "打手申请不存在");
    ErrorCode WORKER_APPLICATION_ALREADY_PENDING = new ErrorCode(1_020_002_001, "已有待审核的申请，请勿重复提交");
    ErrorCode WORKER_APPLICATION_STATUS_ERROR = new ErrorCode(1_020_002_002, "申请状态不允许此操作");
    ErrorCode WORKER_APPLICATION_REJECT_REASON_REQUIRED = new ErrorCode(1_020_002_003, "驳回原因不能为空");

    // ========== 打手技能 1-020-003-xxx ==========
    ErrorCode WORKER_SKILL_EXISTS = new ErrorCode(1_020_003_000, "该技能已存在");
    ErrorCode WORKER_SKILL_DUPLICATE = new ErrorCode(1_020_003_001, "技能存在重复（同设备+服务类型不可重复）");
    ErrorCode WORKER_SKILL_EMPTY = new ErrorCode(1_020_003_002, "技能列表不能为空");
    ErrorCode WORKER_SKILL_WORKER_NOT_EXISTS = new ErrorCode(1_020_003_003, "指定打手不存在");

    // ========== 打手工作状态 1-020-009-xxx ==========
    ErrorCode WORKER_WORK_STATUS_INVALID = new ErrorCode(1_020_009_000, "工作状态值无效");
    ErrorCode WORKER_CANNOT_GO_ONLINE = new ErrorCode(1_020_009_001, "不满足上线条件（需审核通过、启用且有技能）");
    ErrorCode WORKER_PROFILE_UPDATE_FIELD_INVALID = new ErrorCode(1_020_009_002, "不允许修改该字段");

    // ========== 商品服务配置 1-020-004-xxx ==========
    ErrorCode PRODUCT_SERVICE_CONFIG_NOT_EXISTS = new ErrorCode(1_020_004_000, "商品服务配置不存在");
    ErrorCode PRODUCT_SERVICE_CONFIG_DISABLED = new ErrorCode(1_020_004_001, "商品服务配置已禁用");
    ErrorCode PRODUCT_SERVICE_CONFIG_DUPLICATE = new ErrorCode(1_020_004_002, "商品服务配置重复（相同SPU+SKU不可重复）");
    ErrorCode PRODUCT_SERVICE_CONFIG_SKU_SPU_REQUIRED = new ErrorCode(1_020_004_003, "SPU ID和SKU ID至少填写一个");
    ErrorCode PRODUCT_SERVICE_CONFIG_TENANT_MISMATCH = new ErrorCode(1_020_004_004, "商品服务配置不属于当前租户");

    // ========== 服务订单 1-020-005-xxx ==========
    ErrorCode SERVICE_ORDER_NOT_EXISTS = new ErrorCode(1_020_005_000, "服务订单不存在");
    ErrorCode SERVICE_ORDER_ALREADY_EXISTS = new ErrorCode(1_020_005_001, "服务订单已存在（重复创建）");
    ErrorCode SERVICE_ORDER_STATUS_INVALID = new ErrorCode(1_020_005_002, "服务订单状态不允许此操作");
    ErrorCode SERVICE_ORDER_ALREADY_CLAIMED = new ErrorCode(1_020_005_003, "服务订单已被其他打手接走");
    ErrorCode SERVICE_ORDER_NOT_BELONG_TO_WORKER = new ErrorCode(1_020_005_004, "该订单不属于当前打手");
    ErrorCode SERVICE_ORDER_NOT_BELONG_TO_USER = new ErrorCode(1_020_005_005, "服务订单不属于当前会员");
    ErrorCode SERVICE_ORDER_NO_GENERATE_FAILED = new ErrorCode(1_020_005_006, "服务订单号生成失败");

    // ========== 派单 1-020-006-xxx ==========
    ErrorCode ASSIGNMENT_WORKER_NOT_AVAILABLE = new ErrorCode(1_020_006_000, "指定打手不可用");
    ErrorCode ASSIGNMENT_ALREADY_EXISTS = new ErrorCode(1_020_006_001, "已有有效派单记录");
    ErrorCode ASSIGNMENT_NOT_FOUND = new ErrorCode(1_020_006_002, "分配记录不存在");
    ErrorCode ASSIGNMENT_WORKER_NOT_ONLINE = new ErrorCode(1_020_006_003, "打手不在线");
    ErrorCode ASSIGNMENT_WORKER_BUSY = new ErrorCode(1_020_006_004, "打手已忙碌");
    ErrorCode ASSIGNMENT_WORKER_PAUSED = new ErrorCode(1_020_006_005, "打手已暂停接单");
    ErrorCode ASSIGNMENT_WORKER_SKILL_NOT_MATCH = new ErrorCode(1_020_006_006, "打手技能不匹配");
    ErrorCode ASSIGNMENT_WORKER_HAS_ACTIVE_ORDER = new ErrorCode(1_020_006_007, "打手已有进行中订单");
    ErrorCode ASSIGNMENT_ORDER_ALREADY_STARTED = new ErrorCode(1_020_006_008, "订单已开始服务，不能改派或退回");
    ErrorCode ASSIGNMENT_REASON_REQUIRED = new ErrorCode(1_020_006_009, "操作原因不能为空");
    ErrorCode ASSIGNMENT_ORDER_ALREADY_CONFIRMED = new ErrorCode(1_020_006_010, "订单已确认，请勿重复操作");
    ErrorCode ASSIGNMENT_ORDER_BEING_PROCESSED = new ErrorCode(1_020_006_011, "订单正在处理中，请稍后重试");
    ErrorCode ASSIGNMENT_CROSS_TENANT = new ErrorCode(1_020_006_012, "跨租户操作被拒绝");
    ErrorCode ASSIGNMENT_NO_WORKER_IDENTITY = new ErrorCode(1_020_006_013, "当前会员不是打手");

    // ========== 凭证 1-020-007-xxx ==========
    ErrorCode EVIDENCE_NOT_EXISTS = new ErrorCode(1_020_007_000, "完成凭证不存在");
    ErrorCode EVIDENCE_ALREADY_REVIEWED = new ErrorCode(1_020_007_001, "完成凭证已审核");

    // ========== 结算 1-020-008-xxx ==========
    ErrorCode SETTLEMENT_NOT_EXISTS = new ErrorCode(1_020_008_000, "结算记录不存在");
    ErrorCode SETTLEMENT_ALREADY_EXISTS = new ErrorCode(1_020_008_001, "结算记录已存在");
    ErrorCode SETTLEMENT_STATUS_INVALID = new ErrorCode(1_020_008_002, "结算状态不允许此操作");
    ErrorCode SETTLEMENT_SERVICE_ORDER_NOT_COMPLETED = new ErrorCode(1_020_008_003, "服务单尚未完成，不能生成结算");
    ErrorCode SETTLEMENT_NO_VALID_WORKER = new ErrorCode(1_020_008_004, "服务单没有有效打手");
    ErrorCode SETTLEMENT_COMMISSION_RATE_NOT_CONFIGURED = new ErrorCode(1_020_008_005, "抽成比例未配置");
    ErrorCode SETTLEMENT_COMMISSION_RATE_INVALID = new ErrorCode(1_020_008_006, "抽成比例不合法");
    ErrorCode SETTLEMENT_AMOUNT_CALCULATE_FAILED = new ErrorCode(1_020_008_007, "结算金额计算失败");
    ErrorCode SETTLEMENT_CANNOT_APPROVE = new ErrorCode(1_020_008_008, "结算状态不允许审核通过（仅待审核可操作）");
    ErrorCode SETTLEMENT_CANNOT_REJECT = new ErrorCode(1_020_008_009, "结算状态不允许审核驳回（仅待审核可操作）");
    ErrorCode SETTLEMENT_CANNOT_RESUBMIT = new ErrorCode(1_020_008_010, "结算状态不允许重新提交（仅已驳回可操作）");
    ErrorCode SETTLEMENT_CANNOT_MARK_PAID = new ErrorCode(1_020_008_011, "结算状态不允许标记已打款（仅审核通过可操作）");
    ErrorCode SETTLEMENT_CANNOT_REVOKE_PAID = new ErrorCode(1_020_008_012, "结算状态不允许撤销打款（仅已打款可操作）");
    ErrorCode SETTLEMENT_REJECT_REASON_EMPTY = new ErrorCode(1_020_008_013, "审核驳回原因不能为空");
    ErrorCode SETTLEMENT_REVOKE_REASON_EMPTY = new ErrorCode(1_020_008_014, "撤销打款原因不能为空");
    ErrorCode SETTLEMENT_REMARK_TOO_LONG = new ErrorCode(1_020_008_015, "备注过长");
    ErrorCode SETTLEMENT_STATUS_CHANGED = new ErrorCode(1_020_008_016, "结算状态已变化，请刷新后重试");
    ErrorCode SETTLEMENT_CROSS_TENANT = new ErrorCode(1_020_008_017, "跨租户操作被拒绝");
    ErrorCode SETTLEMENT_NOT_BELONG_TO_WORKER = new ErrorCode(1_020_008_018, "无权查看该结算");

    // ========== Phase 5 服务执行 1-020-010-xxx ==========
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_START = new ErrorCode(1_020_010_000, "服务单状态不允许开始服务");
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_PROGRESS = new ErrorCode(1_020_010_001, "服务单状态不允许提交进度");
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_EVIDENCE = new ErrorCode(1_020_010_002, "服务单状态不允许登记凭证");
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_COMPLETE = new ErrorCode(1_020_010_003, "服务单状态不允许提交完成");
    ErrorCode SERVICE_ORDER_ALREADY_STARTED = new ErrorCode(1_020_010_004, "服务单已经开始服务");
    ErrorCode SERVICE_ORDER_ALREADY_COMPLETED = new ErrorCode(1_020_010_005, "服务单已经提交完成");
    ErrorCode PROGRESS_CONTENT_EMPTY = new ErrorCode(1_020_010_006, "进度内容不能为空");
    ErrorCode PROGRESS_PERCENT_INVALID = new ErrorCode(1_020_010_007, "进度百分比不合法(0-100)");
    ErrorCode PROGRESS_TYPE_FORBIDDEN = new ErrorCode(1_020_010_008, "不允许指定系统关键进度类型");
    ErrorCode EVIDENCE_NOT_EXISTS_P5 = new ErrorCode(1_020_010_009, "凭证不存在");
    ErrorCode EVIDENCE_NOT_BELONG_TO_WORKER = new ErrorCode(1_020_010_010, "凭证不属于当前打手");
    ErrorCode EVIDENCE_URL_EMPTY = new ErrorCode(1_020_010_011, "凭证文件地址不能为空");
    ErrorCode EVIDENCE_COUNT_EXCEED = new ErrorCode(1_020_010_012, "凭证数量超过限制");
    ErrorCode EVIDENCE_NO_COMPLETION_EVIDENCE = new ErrorCode(1_020_010_013, "没有完成凭证不能提交完成");
    ErrorCode COMPLETION_SUMMARY_EMPTY = new ErrorCode(1_020_010_014, "完成总结不能为空");
    ErrorCode EVIDENCE_CANNOT_OPERATE_AFTER_COMPLETE = new ErrorCode(1_020_010_015, "已提交完成不能新增或删除凭证");
    ErrorCode SERVICE_ORDER_STATUS_CHANGED = new ErrorCode(1_020_010_016, "服务单状态已变化，请刷新后重试");

    // ========== Phase 6 验收与返工 1-020-011-xxx ==========
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_ACCEPT = new ErrorCode(1_020_011_000, "服务单状态不允许验收");
    ErrorCode SERVICE_ORDER_STATUS_CANNOT_REWORK = new ErrorCode(1_020_011_001, "服务单状态不允许返工");
    ErrorCode SERVICE_ORDER_ALREADY_ACCEPTED = new ErrorCode(1_020_011_002, "服务单已经验收通过");
    ErrorCode SERVICE_ORDER_ALREADY_REWORKED = new ErrorCode(1_020_011_003, "服务单已经要求返工");
    ErrorCode ACCEPTANCE_REMARK_TOO_LONG = new ErrorCode(1_020_011_004, "验收备注过长");
    ErrorCode REWORK_REASON_EMPTY = new ErrorCode(1_020_011_005, "返工原因不能为空");
    ErrorCode REWORK_REASON_TOO_LONG = new ErrorCode(1_020_011_006, "返工原因过长");
    ErrorCode REWORK_COUNT_EXCEED = new ErrorCode(1_020_011_007, "返工次数超过限制");
    ErrorCode ACCEPTANCE_NO_VALID_ASSIGNMENT = new ErrorCode(1_020_011_008, "服务单没有有效分配记录");
    ErrorCode ACCEPTANCE_RECORD_ALREADY_EXISTS = new ErrorCode(1_020_011_009, "验收记录已经存在");
    ErrorCode CROSS_TENANT_ACCESS_DENIED = new ErrorCode(1_020_011_010, "跨租户访问被拒绝");

    // ========== Phase 8 取消申请 1-020-012-xxx ==========
    ErrorCode CANCEL_NOT_EXISTS = new ErrorCode(1_020_012_000, "取消申请不存在");
    ErrorCode CANCEL_STATUS_INVALID = new ErrorCode(1_020_012_001, "取消申请状态不允许此操作");
    ErrorCode CANCEL_ORDER_STATUS_NOT_ALLOWED = new ErrorCode(1_020_012_002, "服务单状态不允许取消");
    ErrorCode CANCEL_ALREADY_PENDING = new ErrorCode(1_020_012_003, "服务单已存在待处理取消申请");
    ErrorCode CANCEL_REJECT_REASON_EMPTY = new ErrorCode(1_020_012_004, "取消驳回原因不能为空");
    ErrorCode CANCEL_REFUND_AMOUNT_INVALID = new ErrorCode(1_020_012_005, "取消退款金额不合法");
    ErrorCode CANCEL_NOT_BELONG_TO_USER = new ErrorCode(1_020_012_006, "无权查看该取消申请");

    // ========== Phase 8 售后案件 1-020-013-xxx ==========
    ErrorCode AFTER_SALE_NOT_EXISTS = new ErrorCode(1_020_013_000, "售后案件不存在");
    ErrorCode AFTER_SALE_ORDER_STATUS_NOT_ALLOWED = new ErrorCode(1_020_013_001, "服务单状态不允许申请售后");
    ErrorCode AFTER_SALE_ALREADY_ACTIVE = new ErrorCode(1_020_013_002, "服务单已存在进行中的售后案件");
    ErrorCode AFTER_SALE_TIME_EXCEED = new ErrorCode(1_020_013_003, "售后申请超过时间限制（完成后7天内）");
    ErrorCode AFTER_SALE_REASON_EMPTY = new ErrorCode(1_020_013_004, "售后申请原因不能为空");
    ErrorCode AFTER_SALE_REFUND_AMOUNT_INVALID = new ErrorCode(1_020_013_005, "请求退款金额不合法（不能超过服务金额）");
    ErrorCode AFTER_SALE_STATUS_CANNOT_ACCEPT = new ErrorCode(1_020_013_006, "售后状态不允许受理（仅待处理可操作）");
    ErrorCode AFTER_SALE_STATUS_CANNOT_REJECT = new ErrorCode(1_020_013_007, "售后状态不允许驳回（仅待处理或已受理可操作）");
    ErrorCode AFTER_SALE_STATUS_CANNOT_ARBITRATE = new ErrorCode(1_020_013_008, "售后状态不允许仲裁（仅待处理或已受理可操作）");
    ErrorCode AFTER_SALE_STATUS_CANNOT_CLOSE = new ErrorCode(1_020_013_009, "售后状态不允许关闭");
    ErrorCode AFTER_SALE_NOT_BELONG_TO_USER = new ErrorCode(1_020_013_010, "无权查看该售后案件");
    ErrorCode AFTER_SALE_REJECT_REASON_EMPTY = new ErrorCode(1_020_013_011, "售后驳回原因不能为空");
    ErrorCode AFTER_SALE_ALREADY_PROCESSED = new ErrorCode(1_020_013_012, "售后案件已经处理完成");

    // ========== Phase 8 仲裁 1-020-014-xxx ==========
    ErrorCode ARBITRATION_DECISION_INVALID = new ErrorCode(1_020_014_000, "仲裁决定类型不合法");
    ErrorCode ARBITRATION_AMOUNT_INVALID = new ErrorCode(1_020_014_001, "仲裁退款金额不合法");
    ErrorCode ARBITRATION_AMOUNT_SUM_INVALID = new ErrorCode(1_020_014_002, "责任金额合计不合法（workerDeduction+platformBear不能超过退款金额）");
    ErrorCode ARBITRATION_ALREADY_EXISTS = new ErrorCode(1_020_014_003, "该售后案件已有仲裁记录");

    // ========== Phase 8 退款记录 1-020-015-xxx ==========
    ErrorCode REFUND_RECORD_ALREADY_EXISTS = new ErrorCode(1_020_015_000, "退款记录已存在");
    ErrorCode REFUND_RECORD_NOT_EXISTS = new ErrorCode(1_020_015_001, "退款记录不存在");

    // ========== Phase 8 结算联动 1-020-016-xxx ==========
    ErrorCode SETTLEMENT_PAID_NEED_MANUAL_RECOVERY = new ErrorCode(1_020_016_000, "已打款结算需要人工追回，不能自动取消");

    // ========== Phase 9 退款处理 1-020-017-xxx ==========
    ErrorCode REFUND_STATUS_CANNOT_START = new ErrorCode(1_020_017_000, "退款状态不允许开始处理");
    ErrorCode REFUND_STATUS_CANNOT_COMPLETE = new ErrorCode(1_020_017_001, "退款状态不允许完成");
    ErrorCode REFUND_STATUS_CANNOT_FAIL = new ErrorCode(1_020_017_002, "退款状态不允许标记失败");
    ErrorCode REFUND_STATUS_CANNOT_RETRY = new ErrorCode(1_020_017_003, "退款状态不允许重试");
    ErrorCode REFUND_STATUS_CANNOT_CANCEL = new ErrorCode(1_020_017_004, "退款状态不允许撤销");
    ErrorCode REFUND_METHOD_INVALID = new ErrorCode(1_020_017_005, "人工退款方式不合法");
    ErrorCode REFUND_PROOF_REQUIRED = new ErrorCode(1_020_017_006, "人工退款凭证或参考信息不能为空");
    ErrorCode REFUND_FAIL_REASON_REQUIRED = new ErrorCode(1_020_017_007, "退款失败原因不能为空");
    ErrorCode REFUND_ALREADY_COMPLETED = new ErrorCode(1_020_017_008, "退款已完成，不能重复操作");
    ErrorCode REFUND_STATUS_CHANGED = new ErrorCode(1_020_017_009, "退款状态已变化，请刷新后重试");
    ErrorCode REFUND_CROSS_TENANT = new ErrorCode(1_020_017_010, "跨租户访问被拒绝");
    ErrorCode REFUND_NOT_BELONG_TO_USER = new ErrorCode(1_020_017_011, "退款或追回无权访问");
    ErrorCode REFUND_EXTERNAL_REF_TOO_LONG = new ErrorCode(1_020_017_012, "外部参考号过长");

    // ========== Phase 9 追回任务 1-020-018-xxx ==========
    ErrorCode RECOVERY_NOT_EXISTS = new ErrorCode(1_020_018_000, "追回任务不存在");
    ErrorCode RECOVERY_ALREADY_EXISTS = new ErrorCode(1_020_018_001, "追回任务已经存在");
    ErrorCode RECOVERY_STATUS_CANNOT_START = new ErrorCode(1_020_018_002, "追回状态不允许开始处理");
    ErrorCode RECOVERY_STATUS_CANNOT_RECORD = new ErrorCode(1_020_018_003, "追回状态不允许记录金额");
    ErrorCode RECOVERY_STATUS_CANNOT_FAIL = new ErrorCode(1_020_018_004, "追回状态不允许标记失败");
    ErrorCode RECOVERY_STATUS_CANNOT_RETRY = new ErrorCode(1_020_018_005, "追回状态不允许重试");
    ErrorCode RECOVERY_STATUS_CANNOT_CANCEL = new ErrorCode(1_020_018_006, "追回状态不允许取消");
    ErrorCode RECOVERY_AMOUNT_MUST_POSITIVE = new ErrorCode(1_020_018_007, "追回金额必须大于零");
    ErrorCode RECOVERY_AMOUNT_EXCEED = new ErrorCode(1_020_018_008, "累计追回金额超过应追回金额");
    ErrorCode RECOVERY_HAS_AMOUNT_CANNOT_CANCEL = new ErrorCode(1_020_018_009, "已有追回金额不能取消任务");
    ErrorCode RECOVERY_FAIL_REASON_REQUIRED = new ErrorCode(1_020_018_010, "追回失败原因不能为空");
    ErrorCode RECOVERY_CROSS_TENANT = new ErrorCode(1_020_018_011, "跨租户访问被拒绝");
    ErrorCode RECOVERY_CANCEL_REASON_REQUIRED = new ErrorCode(1_020_018_012, "撤销原因不能为空");
    ErrorCode RECOVERY_ALREADY_COMPLETED = new ErrorCode(1_020_018_013, "追回已完成，不能重复操作");

    // ========== Phase 10 事件与通知 1-020-019-xxx ==========
    ErrorCode EVENT_OUTBOX_NOT_EXISTS = new ErrorCode(1_020_019_000, "Outbox 事件不存在");
    ErrorCode EVENT_OUTBOX_ALREADY_SUCCESS = new ErrorCode(1_020_019_001, "Outbox 事件已经成功");
    ErrorCode EVENT_OUTBOX_STATUS_CANNOT_RETRY = new ErrorCode(1_020_019_002, "Outbox 状态不允许重试");
    ErrorCode EVENT_OUTBOX_STATUS_CANNOT_MARK_DEAD = new ErrorCode(1_020_019_003, "Outbox 状态不允许标记死亡");
    ErrorCode EVENT_PAYLOAD_INVALID = new ErrorCode(1_020_019_004, "事件 Payload 不合法");
    ErrorCode EVENT_TYPE_NOT_SUPPORTED = new ErrorCode(1_020_019_005, "事件类型不支持");
    ErrorCode EVENT_TEMPLATE_NOT_FOUND = new ErrorCode(1_020_019_006, "通知模板不存在");
    ErrorCode EVENT_TEMPLATE_PARAMS_INCOMPLETE = new ErrorCode(1_020_019_007, "通知模板参数不完整");
    ErrorCode EVENT_RECIPIENT_NOT_FOUND = new ErrorCode(1_020_019_008, "通知接收人不存在");
    ErrorCode EVENT_CONSUME_STATUS_CHANGED = new ErrorCode(1_020_019_009, "事件消费状态已变化，请刷新后重试");
    ErrorCode EVENT_EXCEED_MAX_RETRY = new ErrorCode(1_020_019_010, "事件超过最大重试次数");
    ErrorCode EVENT_OUTBOX_STATUS_CHANGED = new ErrorCode(1_020_019_011, "事件状态已变化");

    // ========== Phase 10 通知消息 1-020-020-xxx ==========
    ErrorCode NOTIFICATION_NOT_EXISTS = new ErrorCode(1_020_020_000, "站内消息不存在");
    ErrorCode NOTIFICATION_NOT_BELONG_TO_USER = new ErrorCode(1_020_020_001, "无权查看该消息");
    ErrorCode NOTIFICATION_ALREADY_READ = new ErrorCode(1_020_020_002, "消息已经已读");

    // ========== Phase 10 提醒 1-020-021-xxx ==========
    ErrorCode REMINDER_RECORD_EXISTS = new ErrorCode(1_020_021_000, "提醒记录已经存在");

    // ========== 运营统计 1-020-022-xxx ==========
    ErrorCode STATISTICS_DATE_RANGE_INVALID = new ErrorCode(1_020_022_000, "时间范围不合法");
    ErrorCode STATISTICS_GRANULARITY_INVALID = new ErrorCode(1_020_022_001, "统计粒度不合法（仅支持DAY/WEEK/MONTH）");
    ErrorCode STATISTICS_RANGE_TOO_LARGE = new ErrorCode(1_020_022_002, "统计时间范围不能超过366天");

    // ========== 财务对账 1-020-023-xxx ==========
    ErrorCode FINANCE_RECONCILIATION_NOT_EXISTS = new ErrorCode(1_020_023_000, "对账记录不存在");
    ErrorCode FINANCE_RECONCILIATION_DUPLICATE = new ErrorCode(1_020_023_001, "该日期已存在对账记录，不能重复生成");
    ErrorCode FINANCE_RECONCILIATION_STATUS_NOT_ALLOWED = new ErrorCode(1_020_023_002, "对账状态不允许此操作");
    ErrorCode FINANCE_RECONCILIATION_CALCULATE_FAILED = new ErrorCode(1_020_023_003, "对账计算失败");
    ErrorCode FINANCE_RECONCILIATION_ALREADY_CONFIRMED = new ErrorCode(1_020_023_004, "对账已确认，不能重复操作");
    ErrorCode FINANCE_RECONCILIATION_ALREADY_CANCELED = new ErrorCode(1_020_023_005, "对账已取消");
    ErrorCode FINANCE_RECONCILIATION_CONFIRM_REMARK_TOO_LONG = new ErrorCode(1_020_023_006, "确认备注过长（最大500字）");
    ErrorCode FINANCE_RECONCILIATION_CANCEL_REASON_REQUIRED = new ErrorCode(1_020_023_007, "取消原因不能为空");
    ErrorCode FINANCE_RECONCILIATION_CANCEL_REASON_TOO_LONG = new ErrorCode(1_020_023_008, "取消原因过长（最大500字）");
    ErrorCode FINANCE_RECONCILIATION_DATE_REQUIRED = new ErrorCode(1_020_023_009, "对账日期不能为空");
    ErrorCode FINANCE_RECONCILIATION_EXPORT_OVER_LIMIT = new ErrorCode(1_020_023_010, "导出数据超过最大限制（最多10000条）");

    // ========== Phase 9 俱乐部入驻申请 1-020-024-xxx ==========
    ErrorCode CLUB_APPLICATION_NOT_EXISTS = new ErrorCode(1_020_024_000, "俱乐部入驻申请不存在");
    ErrorCode CLUB_APPLICATION_PENDING_EXISTS = new ErrorCode(1_020_024_001, "已有待审核的入驻申请，请勿重复提交");
    ErrorCode CLUB_APPLICATION_STATUS_NOT_ALLOWED = new ErrorCode(1_020_024_002, "入驻申请状态不允许此操作");
    ErrorCode CLUB_APPLICATION_ALREADY_APPROVED = new ErrorCode(1_020_024_003, "入驻申请已通过");
    ErrorCode CLUB_APPLICATION_ALREADY_REJECTED = new ErrorCode(1_020_024_004, "入驻申请已拒绝");
    ErrorCode CLUB_APPLICATION_ALREADY_CANCELED = new ErrorCode(1_020_024_005, "入驻申请已撤销");
    ErrorCode CLUB_APPLICATION_REJECT_REASON_REQUIRED = new ErrorCode(1_020_024_006, "拒绝原因不能为空");
    ErrorCode CLUB_APPLICATION_TENANT_ID_REQUIRED = new ErrorCode(1_020_024_007, "审核通过时必须指定已有租户ID");
    ErrorCode CLUB_APPLICATION_SUBMIT_BUSY = new ErrorCode(1_020_024_008, "提交过于频繁，请稍后重试");

    // ========== Phase 9 俱乐部管理 1-020-025-xxx ==========
    ErrorCode CLUB_NOT_EXISTS = new ErrorCode(1_020_025_000, "俱乐部不存在");
    ErrorCode CLUB_TENANT_DUPLICATE = new ErrorCode(1_020_025_001, "该租户已存在俱乐部档案");
    ErrorCode CLUB_CODE_DUPLICATE = new ErrorCode(1_020_025_002, "俱乐部编码已存在");
    ErrorCode CLUB_OWNER_DUPLICATE = new ErrorCode(1_020_025_003, "该会员已是其他俱乐部的所有者");
    ErrorCode CLUB_STATUS_NOT_ALLOWED = new ErrorCode(1_020_025_004, "俱乐部经营状态不允许此操作");
    ErrorCode CLUB_COMMISSION_RATE_INVALID = new ErrorCode(1_020_025_005, "平台抽成比例不合法（万分制 0-10000）");
    ErrorCode CLUB_SERVICE_SCOPE_INVALID = new ErrorCode(1_020_025_006, "服务范围包含无效的服务类型");
    ErrorCode CLUB_SERVICE_SCOPE_EMPTY = new ErrorCode(1_020_025_007, "服务范围不能为空");
    ErrorCode CLUB_UPDATE_FIELD_NOT_ALLOWED = new ErrorCode(1_020_025_008, "不允许修改该字段");
    ErrorCode CLUB_STATUS_REASON_REQUIRED = new ErrorCode(1_020_025_009, "状态变更原因不能为空");

    // ========== Phase 10 订单市场 1-020-026-xxx ==========
    ErrorCode ORDER_MARKET_LISTING_NOT_EXISTS = new ErrorCode(1_020_026_000, "挂牌不存在");
    ErrorCode ORDER_MARKET_ORDER_NOT_EXISTS = new ErrorCode(1_020_026_001, "服务订单不存在");
    ErrorCode ORDER_MARKET_ORDER_STATUS_NOT_ALLOWED = new ErrorCode(1_020_026_002, "服务订单状态不允许发布市场");
    ErrorCode ORDER_MARKET_ALREADY_PUBLISHED = new ErrorCode(1_020_026_003, "服务订单已有有效挂牌");
    ErrorCode ORDER_MARKET_STATUS_NOT_ALLOWED = new ErrorCode(1_020_026_004, "挂牌状态不允许此操作");
    ErrorCode ORDER_MARKET_ALREADY_CLAIMED = new ErrorCode(1_020_026_005, "挂牌已被其他俱乐部接单");
    ErrorCode ORDER_MARKET_EXPIRED = new ErrorCode(1_020_026_006, "挂牌已过期");
    ErrorCode ORDER_MARKET_CLAIM_BUSY = new ErrorCode(1_020_026_007, "订单正在处理中，请稍后重试");
    ErrorCode ORDER_MARKET_CLUB_NOT_EXISTS = new ErrorCode(1_020_026_008, "当前租户没有俱乐部档案");
    ErrorCode ORDER_MARKET_CLUB_DISABLED = new ErrorCode(1_020_026_009, "俱乐部经营状态已停用");
    ErrorCode ORDER_MARKET_SERVICE_SCOPE_NOT_MATCH = new ErrorCode(1_020_026_010, "俱乐部服务范围不包含此订单服务类型");
    ErrorCode ORDER_MARKET_CLUB_CAPACITY_FULL = new ErrorCode(1_020_026_011, "俱乐部当前进行中订单已达上限");
    ErrorCode ORDER_MARKET_WITHDRAW_REASON_REQUIRED = new ErrorCode(1_020_026_012, "撤回原因不能为空");
    ErrorCode ORDER_MARKET_ASSIGN_CLUB_INVALID = new ErrorCode(1_020_026_013, "指定俱乐部无效或不符合条件");
    ErrorCode ORDER_MARKET_CAS_FAILED = new ErrorCode(1_020_026_014, "操作失败，挂牌状态已变化，请刷新后重试");
    ErrorCode ORDER_MARKET_ORDER_ALREADY_ASSIGNED = new ErrorCode(1_020_026_015, "服务订单已分配俱乐部");

}
