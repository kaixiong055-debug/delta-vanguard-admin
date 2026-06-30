-- ================================================
-- Delta Vanguard Phase 7: 打手结算生成与审核
-- ================================================

-- 1. 修改 delta_worker_settlement 表，新增审核/打款字段
ALTER TABLE delta_worker_settlement
    ADD COLUMN reviewer_id  bigint       NULL     DEFAULT NULL COMMENT '审核人ID（AdminUser）' AFTER pay_reference,
    ADD COLUMN review_time  datetime     NULL     DEFAULT NULL COMMENT '审核时间' AFTER reviewer_id,
    ADD COLUMN reject_reason varchar(512) NULL    DEFAULT '' COMMENT '驳回原因' AFTER review_time,
    ADD COLUMN payer_id     bigint       NULL     DEFAULT NULL COMMENT '打款人ID（AdminUser）' AFTER reject_reason,
    ADD COLUMN paid_time    datetime     NULL     DEFAULT NULL COMMENT '打款时间' AFTER payer_id,
    ADD COLUMN pay_method   int          NULL     DEFAULT NULL COMMENT '打款方式' AFTER paid_time,
    ADD COLUMN pay_remark   varchar(255) NULL     DEFAULT '' COMMENT '打款备注' AFTER pay_method;

-- 2. 新增结算操作日志表
CREATE TABLE IF NOT EXISTS delta_worker_settlement_log
(
    id               bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id        bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    settlement_id    bigint       NOT NULL COMMENT '结算单ID',
    service_order_id bigint       NOT NULL COMMENT '服务订单ID',
    operation_type   varchar(32)  NOT NULL COMMENT '操作类型（GENERATE/APPROVE/REJECT/RESUBMIT/MARK_PAID/REVOKE_PAID）',
    before_status    int          NULL     DEFAULT NULL COMMENT '操作前状态',
    after_status     int          NULL     DEFAULT NULL COMMENT '操作后状态',
    operator_type    tinyint      NOT NULL COMMENT '操作人类型：1-会员 3-管理员 4-系统',
    operator_id      bigint       NULL     DEFAULT NULL COMMENT '操作人ID',
    content          varchar(500) NULL     DEFAULT '' COMMENT '操作内容',
    amount_snapshot  varchar(500) NULL     DEFAULT '' COMMENT '金额快照JSON',
    creator          varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_settlement_time (tenant_id, settlement_id, create_time),
    INDEX idx_tenant_service_order (tenant_id, service_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '结算操作日志表';

-- 3. 补充索引
-- 结算表：已存在 uk_tenant_settlement_no、uk_tenant_service_order、idx_tenant_worker、idx_tenant_status
-- 补充 worker + status 索引（如果不存在）
-- ALTER TABLE delta_worker_settlement ADD INDEX idx_tenant_worker_status (tenant_id, worker_id, settlement_status);
