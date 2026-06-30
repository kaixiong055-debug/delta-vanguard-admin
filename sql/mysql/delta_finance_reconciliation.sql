-- ============================================================
-- Phase 8: 财务对账表
-- ============================================================

DROP TABLE IF EXISTS `delta_finance_reconciliation`;
CREATE TABLE `delta_finance_reconciliation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `reconciliation_no` VARCHAR(64) NOT NULL COMMENT '对账单号',
    `reconciliation_date` DATE NOT NULL COMMENT '对账日期',
    `period_start_time` DATETIME NOT NULL COMMENT '对账周期开始',
    `period_end_time` DATETIME NOT NULL COMMENT '对账周期结束',

    -- 服务订单汇总
    `service_order_count` INT DEFAULT 0 COMMENT '服务订单数',
    `service_order_amount` BIGINT DEFAULT 0 COMMENT '服务订单金额（分）',

    -- 结算汇总
    `settlement_count` INT DEFAULT 0 COMMENT '结算笔数',
    `settlement_amount` BIGINT DEFAULT 0 COMMENT '结算金额（分）',
    `paid_settlement_amount` BIGINT DEFAULT 0 COMMENT '已打款金额（分）',

    -- 退款汇总
    `refund_count` INT DEFAULT 0 COMMENT '退款笔数',
    `refund_amount` BIGINT DEFAULT 0 COMMENT '退款金额（分）',

    -- 追回汇总
    `recovery_count` INT DEFAULT 0 COMMENT '追回笔数',
    `should_recover_amount` BIGINT DEFAULT 0 COMMENT '应追回金额（分）',
    `recovered_amount` BIGINT DEFAULT 0 COMMENT '已追回金额（分）',

    -- 平台金额
    `expected_platform_amount` BIGINT DEFAULT 0 COMMENT '预期平台收入（分）',
    `actual_platform_amount` BIGINT DEFAULT 0 COMMENT '实际平台收入（分）',
    `difference_amount` BIGINT DEFAULT 0 COMMENT '差异金额（分）',

    -- 状态
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消',
    `failure_reason` VARCHAR(1024) DEFAULT NULL COMMENT '失败原因',
    `confirm_remark` VARCHAR(512) DEFAULT NULL COMMENT '确认备注',
    `confirmer_id` BIGINT DEFAULT NULL COMMENT '确认人ID',
    `confirmed_time` DATETIME DEFAULT NULL COMMENT '确认时间',
    `calculate_time` DATETIME DEFAULT NULL COMMENT '计算完成时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 租户 & 审计
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_reconciliation_date` (`tenant_id`, `reconciliation_date`, `deleted`),
    KEY `idx_reconciliation_no` (`reconciliation_no`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务对账记录';
