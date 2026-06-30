-- =====================================================
-- Delta 先锋俱乐部 Phase 8：取消、售后、仲裁、退款
-- =====================================================

-- 取消申请表
CREATE TABLE IF NOT EXISTS delta_order_cancel
(
    id                  bigint       NOT NULL AUTO_INCREMENT,
    tenant_id           bigint       NOT NULL DEFAULT 0,
    cancel_no           varchar(32)  NOT NULL COMMENT '取消单号',
    service_order_id    bigint       NOT NULL COMMENT '服务订单ID',
    buyer_user_id       bigint       DEFAULT NULL COMMENT '买家用户ID',
    worker_id           bigint       DEFAULT NULL COMMENT '打手ID',
    apply_reason        varchar(512) DEFAULT '' COMMENT '申请原因',
    apply_remark        varchar(512) DEFAULT '' COMMENT '申请备注',
    apply_status        tinyint      NOT NULL DEFAULT 0 COMMENT '取消状态：0-待审核 1-已通过 2-已驳回',
    original_order_status int        DEFAULT NULL COMMENT '原始服务单状态（快照）',
    cancel_type         tinyint      DEFAULT 1 COMMENT '取消类型：1-买家取消',
    refund_amount       int          DEFAULT 0 COMMENT '退款金额（分）',
    responsibility_type tinyint      DEFAULT NULL COMMENT '责任归属',
    reviewer_id         bigint       DEFAULT NULL COMMENT '审核人ID',
    review_time         datetime     DEFAULT NULL COMMENT '审核时间',
    review_remark       varchar(512) DEFAULT '' COMMENT '审核备注',
    creator             varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_cancel_no (tenant_id, cancel_no),
    INDEX idx_tenant_service_order (tenant_id, service_order_id),
    INDEX idx_tenant_buyer (tenant_id, buyer_user_id),
    INDEX idx_tenant_status (tenant_id, apply_status)
) COMMENT '取消申请表';

-- 售后案件表
CREATE TABLE IF NOT EXISTS delta_after_sale
(
    id                     bigint       NOT NULL AUTO_INCREMENT,
    tenant_id              bigint       NOT NULL DEFAULT 0,
    after_sale_no          varchar(32)  NOT NULL COMMENT '售后单号',
    service_order_id       bigint       NOT NULL COMMENT '服务订单ID',
    buyer_user_id          bigint       DEFAULT NULL COMMENT '买家用户ID',
    worker_id              bigint       DEFAULT NULL COMMENT '打手ID',
    after_sale_type        tinyint      DEFAULT NULL COMMENT '售后类型',
    reason_type            tinyint      DEFAULT NULL COMMENT '原因类型',
    reason                 varchar(512) DEFAULT '' COMMENT '申请原因',
    description            varchar(1024) DEFAULT '' COMMENT '问题描述',
    evidence_urls          varchar(2048) DEFAULT '' COMMENT '凭证图片URLs（JSON数组）',
    status                 tinyint      NOT NULL DEFAULT 0 COMMENT '售后状态：0-待处理 1-已受理 2-已驳回 3-已仲裁 4-已关闭',
    original_order_status  int          DEFAULT NULL COMMENT '原始服务单状态（快照）',
    requested_refund_amount int         DEFAULT 0 COMMENT '请求退款金额（分）',
    approved_refund_amount int          DEFAULT 0 COMMENT '批准退款金额（分）',
    responsibility_type    tinyint      DEFAULT NULL COMMENT '责任归属',
    handler_id             bigint       DEFAULT NULL COMMENT '处理人ID',
    handle_time            datetime     DEFAULT NULL COMMENT '处理时间',
    handle_remark          varchar(512) DEFAULT '' COMMENT '处理备注',
    need_manual_recovery    tinyint      DEFAULT 0 COMMENT '需要人工追回（已打款结算时标记）',
    creator                varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_after_sale_no (tenant_id, after_sale_no),
    INDEX idx_tenant_service_order (tenant_id, service_order_id),
    INDEX idx_tenant_buyer (tenant_id, buyer_user_id),
    INDEX idx_tenant_status (tenant_id, status)
) COMMENT '售后案件表';

-- 仲裁记录表
CREATE TABLE IF NOT EXISTS delta_after_sale_arbitration
(
    id                       bigint      NOT NULL AUTO_INCREMENT,
    tenant_id                bigint      NOT NULL DEFAULT 0,
    after_sale_id            bigint      NOT NULL COMMENT '售后案件ID',
    service_order_id         bigint      NOT NULL COMMENT '服务订单ID',
    decision_type            tinyint     NOT NULL COMMENT '仲裁决定类型：0-不退款 1-全额退款 2-部分退款 3-继续服务',
    refund_amount            int         DEFAULT 0 COMMENT '退款金额（分）',
    worker_deduction_amount  int         DEFAULT 0 COMMENT '打手扣减金额（分）',
    platform_bear_amount     int         DEFAULT 0 COMMENT '平台承担金额（分）',
    responsibility_type      tinyint     DEFAULT NULL COMMENT '责任归属',
    operator_id              bigint      DEFAULT NULL COMMENT '操作人ID',
    remark                   varchar(512) DEFAULT '' COMMENT '仲裁备注',
    before_status            int         DEFAULT NULL COMMENT '仲裁前状态',
    after_status             int         DEFAULT NULL COMMENT '仲裁后状态',
    creator                  varchar(64) DEFAULT '' COMMENT '创建者',
    create_time              datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                  varchar(64) DEFAULT '' COMMENT '更新者',
    update_time              datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                  tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    INDEX idx_tenant_after_sale (tenant_id, after_sale_id),
    INDEX idx_tenant_service_order (tenant_id, service_order_id)
) COMMENT '仲裁记录表';

-- 内部退款记录表
CREATE TABLE IF NOT EXISTS delta_refund_record
(
    id                bigint       NOT NULL AUTO_INCREMENT,
    tenant_id         bigint       NOT NULL DEFAULT 0,
    refund_no         varchar(32)  NOT NULL COMMENT '退款单号',
    service_order_id  bigint       NOT NULL COMMENT '服务订单ID',
    after_sale_id     bigint       DEFAULT NULL COMMENT '售后案件ID',
    buyer_user_id     bigint       DEFAULT NULL COMMENT '买家用户ID',
    refund_amount     int          NOT NULL COMMENT '退款金额（分）',
    refund_reason     varchar(512) DEFAULT '' COMMENT '退款原因',
    refund_status     tinyint      NOT NULL DEFAULT 0 COMMENT '退款状态：0-待人工退款 1-人工退款处理中 2-人工退款已完成 3-已取消',
    refund_channel    varchar(32)  DEFAULT '' COMMENT '退款渠道',
    external_refund_no varchar(64) DEFAULT '' COMMENT '外部退款流水号',
    operator_id       bigint       DEFAULT NULL COMMENT '操作人ID',
    remark            varchar(512) DEFAULT '' COMMENT '备注',
    creator           varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_refund_no (tenant_id, refund_no),
    INDEX idx_tenant_after_sale (tenant_id, after_sale_id),
    INDEX idx_tenant_service_order (tenant_id, service_order_id)
) COMMENT '内部退款记录表';
