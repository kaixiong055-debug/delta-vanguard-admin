-- =====================================================
-- Delta 先锋俱乐部 - 建表脚本
-- =====================================================

-- 1. 打手资料表
CREATE TABLE IF NOT EXISTS delta_worker
(
    id                    bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id             bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    user_id               bigint       NOT NULL COMMENT '会员用户ID（关联 member_user.id）',
    worker_no             varchar(32)  NOT NULL COMMENT '打手编号',
    real_name             varchar(32)  NULL     DEFAULT '' COMMENT '真实姓名',
    display_name          varchar(32)  NULL     DEFAULT '' COMMENT '展示名称',
    phone                 varchar(20)  NULL     DEFAULT '' COMMENT '手机号',
    avatar                varchar(255) NULL     DEFAULT '' COMMENT '头像URL',
    audit_status          tinyint      NOT NULL DEFAULT 0 COMMENT '审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑',
    work_status           tinyint      NOT NULL DEFAULT 0 COMMENT '工作状态：0-离线 1-在线 2-忙碌 3-暂停接单',
    level                 tinyint      NOT NULL DEFAULT 1 COMMENT '打手等级：1-初级 2-中级 3-高级 4-资深',
    score                 int          NOT NULL DEFAULT 0 COMMENT '评分（万分制，如49500表示4.95分）',
    commission_rate       int          NULL     DEFAULT 0 COMMENT '抽成比例（万分制，如1500表示15.00%）',
    max_order_count       int          NOT NULL DEFAULT 5 COMMENT '最大同时接单数',
    current_order_count   int          NOT NULL DEFAULT 0 COMMENT '当前进行中订单数',
    completed_order_count int          NOT NULL DEFAULT 0 COMMENT '历史完成订单数',
    cancel_order_count    int          NOT NULL DEFAULT 0 COMMENT '取消订单数',
    is_recommend          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    status                tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0-开启 1-关闭（对应 CommonStatusEnum）',
    audit_remark          varchar(255) NULL     DEFAULT '' COMMENT '审核备注',
    approved_at           datetime     NULL     DEFAULT NULL COMMENT '审核通过时间',
    creator               varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater               varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted               tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_user_id (tenant_id, user_id),
    UNIQUE KEY uk_tenant_worker_no (tenant_id, worker_no),
    INDEX idx_audit_status (tenant_id, audit_status),
    INDEX idx_work_status (tenant_id, work_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '打手资料表';

-- 2. 打手申请表
CREATE TABLE IF NOT EXISTS delta_worker_application
(
    id                  bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id           bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    user_id             bigint       NOT NULL COMMENT '会员用户ID',
    real_name           varchar(32)  NOT NULL COMMENT '真实姓名',
    phone               varchar(20)  NOT NULL COMMENT '手机号',
    game_uid            varchar(64)  NULL     DEFAULT '' COMMENT '游戏账号UID',
    device_type         tinyint      NOT NULL COMMENT '设备类型：1-手机 2-平板 3-PC',
    introduction        varchar(500) NULL     DEFAULT '' COMMENT '个人介绍',
    experience          varchar(500) NULL     DEFAULT '' COMMENT '打手经验描述',
    evidence_urls       varchar(2000) NULL    DEFAULT NULL COMMENT '凭证图片URL列表（JSON数组）',
    check_evidence_url  varchar(255) NULL     DEFAULT '' COMMENT '审核凭证图片URL',
    application_status  tinyint      NOT NULL DEFAULT 0 COMMENT '申请状态：0-待审核 1-审核通过 2-审核驳回',
    reject_reason       varchar(255) NULL     DEFAULT '' COMMENT '驳回原因',
    reviewer_id         bigint       NULL     DEFAULT NULL COMMENT '审核人ID（关联 admin_user.id）',
    reviewed_at         datetime     NULL     DEFAULT NULL COMMENT '审核时间',
    creator             varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_user_status (tenant_id, user_id, application_status),
    INDEX idx_tenant_status (tenant_id, application_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '打手申请表';

-- 3. 打手技能表
CREATE TABLE IF NOT EXISTS delta_worker_skill
(
    id           bigint     NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id    bigint     NOT NULL DEFAULT 0 COMMENT '租户ID',
    worker_id    bigint     NOT NULL COMMENT '打手ID（关联 delta_worker.id）',
    device_type  tinyint    NOT NULL COMMENT '设备类型：1-手机 2-平板 3-PC',
    service_type tinyint    NOT NULL COMMENT '服务类型：1-陪玩 2-护航 3-趣味单',
    skill_level  tinyint    NOT NULL DEFAULT 1 COMMENT '技能等级：1-初级 2-中级 3-高级 4-资深',
    status       tinyint    NOT NULL DEFAULT 0 COMMENT '状态：0-开启 1-关闭（对应 CommonStatusEnum）',
    creator      varchar(64) NULL    DEFAULT '' COMMENT '创建者',
    create_time  datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64) NULL    DEFAULT '' COMMENT '更新者',
    update_time  datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_worker_skill (tenant_id, worker_id, device_type, service_type),
    INDEX idx_tenant_worker (tenant_id, worker_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '打手技能表';

-- 4. 商品服务配置表
CREATE TABLE IF NOT EXISTS delta_product_service_config
(
    id                       bigint     NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id                bigint     NOT NULL DEFAULT 0 COMMENT '租户ID',
    spu_id                   bigint     NOT NULL COMMENT '商品SPU ID（关联 product_spu.id）',
    sku_id                   bigint     NOT NULL COMMENT '商品SKU ID（关联 product_sku.id）',
    service_type             tinyint    NOT NULL COMMENT '服务类型：1-陪玩 2-护航 3-趣味单',
    device_type              tinyint    NOT NULL COMMENT '设备类型：1-手机 2-平板 3-PC',
    required_worker_level    tinyint    NOT NULL DEFAULT 1 COMMENT '要求打手等级：1-初级 2-中级 3-高级 4-资深',
    allow_designated_worker  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许指定打手：0-否 1-是',
    allow_public_claim       tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许大厅抢单：0-否 1-是',
    default_dispatch_mode    tinyint    NOT NULL DEFAULT 3 COMMENT '默认派单方式：1-客户指定 2-客服派单 3-接单大厅',
    max_service_hours        int        NULL     DEFAULT NULL COMMENT '最大服务时长（小时）',
    commission_rate          int        NOT NULL DEFAULT 0 COMMENT '抽成比例（万分制，如1500表示15.00%）',
    enabled                  tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    creator                  varchar(64) NULL   DEFAULT '' COMMENT '创建者',
    create_time              datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                  varchar(64) NULL   DEFAULT '' COMMENT '更新者',
    update_time              datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_sku_id (tenant_id, sku_id),
    INDEX idx_tenant_spu_id (tenant_id, spu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品服务配置表';

-- 5. 服务履约订单表
CREATE TABLE IF NOT EXISTS delta_service_order
(
    id                   bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id            bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    service_order_no     varchar(32)  NOT NULL COMMENT '服务订单号',
    trade_order_id       bigint       NOT NULL COMMENT '商城订单ID（关联 trade_order.id）',
    trade_order_no       varchar(32)  NOT NULL COMMENT '商城订单号',
    trade_order_item_id  bigint       NOT NULL COMMENT '商城订单项ID（关联 trade_order_item.id）',
    buyer_user_id        bigint       NOT NULL COMMENT '买家会员用户ID',
    spu_id               bigint       NOT NULL COMMENT '商品SPU ID',
    sku_id               bigint       NOT NULL COMMENT '商品SKU ID',
    product_name         varchar(128) NOT NULL COMMENT '商品名称（快照）',
    sku_name             varchar(128) NULL     DEFAULT '' COMMENT 'SKU名称（快照）',
    product_pic_url      varchar(500) NULL     DEFAULT '' COMMENT '商品图片（快照）',
    count                int          NOT NULL DEFAULT 1 COMMENT '购买数量（快照）',
    service_type         tinyint      NOT NULL COMMENT '服务类型：1-陪玩 2-护航 3-趣味单',
    device_type          tinyint      NOT NULL COMMENT '设备类型：1-手机 2-平板 3-PC',
    service_amount       int          NOT NULL COMMENT '服务金额（分，快照）',
    dispatch_mode        tinyint      NOT NULL COMMENT '派单方式：1-客户指定 2-客服派单 3-接单大厅',
    preferred_worker_id  bigint       NULL     DEFAULT NULL COMMENT '客户指定打手ID',
    assigned_worker_id   bigint       NULL     DEFAULT NULL COMMENT '最终指派打手ID',
    status               tinyint      NOT NULL COMMENT '服务状态：10-待派单 20-等待指定打手确认 30-接单大厅待领取 40-已接单待开始 50-服务进行中 60-打手已提交完成 70-待客户或客服验收 80-已完成 90-售后处理中 100-纠纷处理中 110-已取消',
    version              int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    customer_remark      varchar(500) NULL     DEFAULT '' COMMENT '客户备注',
    admin_remark         varchar(500) NULL     DEFAULT '' COMMENT '后台备注',
    claim_deadline       datetime     NULL     DEFAULT NULL COMMENT '大厅接单截止时间',
    accepted_at          datetime     NULL     DEFAULT NULL COMMENT '接单时间',
    started_at           datetime     NULL     DEFAULT NULL COMMENT '服务开始时间',
    submitted_at         datetime     NULL     DEFAULT NULL COMMENT '提交完成时间',
    verified_at          datetime     NULL     DEFAULT NULL COMMENT '验收时间',
    completed_at         datetime     NULL     DEFAULT NULL COMMENT '完成时间',
    cancel_reason        varchar(255) NULL     DEFAULT '' COMMENT '取消原因',
    commission_rate      int          NULL     DEFAULT 0 COMMENT '抽成比例（万分制，快照，如1500表示15.00%）',
    platform_fee         int          NULL     DEFAULT 0 COMMENT '平台抽成金额（分）',
    worker_amount        int          NULL     DEFAULT 0 COMMENT '打手收入金额（分）',
    creator              varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater              varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_service_order_no (tenant_id, service_order_no),
    UNIQUE KEY uk_tenant_trade_order_item (tenant_id, trade_order_item_id),
    INDEX idx_tenant_buyer (tenant_id, buyer_user_id),
    INDEX idx_tenant_assigned_worker (tenant_id, assigned_worker_id),
    INDEX idx_tenant_status (tenant_id, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务履约订单表';

-- 6. 派单记录表
CREATE TABLE IF NOT EXISTS delta_order_assignment
(
    id                bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id         bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    service_order_id  bigint       NOT NULL COMMENT '服务订单ID（关联 delta_service_order.id）',
    worker_id         bigint       NOT NULL COMMENT '打手ID（关联 delta_worker.id）',
    assignment_type   tinyint      NOT NULL COMMENT '派单类型：1-客户指定 2-客服派单 3-大厅抢单 4-改派',
    assignment_status tinyint      NOT NULL COMMENT '派单状态：1-待确认 2-已接受 3-已拒绝 4-已超时',
    operator_type     tinyint      NOT NULL COMMENT '操作人类型：1-客户 2-客服 3-系统',
    operator_id       bigint       NOT NULL COMMENT '操作人ID',
    reason            varchar(255) NULL     DEFAULT '' COMMENT '操作原因/备注',
    expired_at        datetime     NULL     DEFAULT NULL COMMENT '过期时间',
    accepted_at       datetime     NULL     DEFAULT NULL COMMENT '接受/拒绝时间',
    creator           varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_service_order (tenant_id, service_order_id),
    INDEX idx_tenant_worker (tenant_id, worker_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '派单记录表';

-- 7. 服务进度表
CREATE TABLE IF NOT EXISTS delta_order_progress
(
    id               bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id        bigint        NOT NULL DEFAULT 0 COMMENT '租户ID',
    service_order_id bigint        NOT NULL COMMENT '服务订单ID',
    worker_id        bigint        NOT NULL COMMENT '打手ID',
    progress_type    tinyint       NOT NULL COMMENT '进度类型：1-开始服务 2-进度更新 3-异常报告',
    progress_percent tinyint       NOT NULL DEFAULT 0 COMMENT '进度百分比（0-100）',
    content          varchar(500)  NULL     DEFAULT '' COMMENT '进度内容',
    image_urls       varchar(2000) NULL     DEFAULT NULL COMMENT '进度图片URL列表（JSON数组）',
    creator          varchar(64)   NULL     DEFAULT '' COMMENT '创建者',
    create_time      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)   NULL     DEFAULT '' COMMENT '更新者',
    update_time      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_service_order (tenant_id, service_order_id, create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务进度表';

-- 8. 完成凭证表
CREATE TABLE IF NOT EXISTS delta_order_evidence
(
    id               bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id        bigint        NOT NULL DEFAULT 0 COMMENT '租户ID',
    service_order_id bigint        NOT NULL COMMENT '服务订单ID',
    worker_id        bigint        NOT NULL COMMENT '打手ID',
    evidence_type    tinyint       NOT NULL COMMENT '凭证类型：1-截图 2-视频 3-文字说明 4-文件',
    content          varchar(500)  NULL     DEFAULT '' COMMENT '凭证内容/说明',
    image_urls       varchar(2000) NULL     DEFAULT NULL COMMENT '凭证图片URL列表（JSON数组）',
    video_url        varchar(255)  NULL     DEFAULT '' COMMENT '凭证视频URL',
    review_status    tinyint       NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核通过 2-审核驳回',
    review_remark    varchar(255)  NULL     DEFAULT '' COMMENT '审核备注',
    creator          varchar(64)   NULL     DEFAULT '' COMMENT '创建者',
    create_time      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)   NULL     DEFAULT '' COMMENT '更新者',
    update_time      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_service_order (tenant_id, service_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '完成凭证表';

-- 9. 打手结算表
CREATE TABLE IF NOT EXISTS delta_worker_settlement
(
    id                bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id         bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    settlement_no     varchar(32)  NOT NULL COMMENT '结算单号',
    service_order_id  bigint       NOT NULL COMMENT '服务订单ID',
    worker_id         bigint       NOT NULL COMMENT '打手ID',
    service_amount    int          NOT NULL COMMENT '服务金额（分）',
    commission_rate   int          NOT NULL COMMENT '抽成比例（万分制，快照）',
    platform_fee      int          NOT NULL DEFAULT 0 COMMENT '平台抽成金额（分）',
    worker_amount     int          NOT NULL COMMENT '打手收入金额（分）',
    settlement_status tinyint      NOT NULL DEFAULT 0 COMMENT '结算状态：0-待结算 1-已结算 2-已冻结 3-已取消',
    settled_at        datetime     NULL     DEFAULT NULL COMMENT '结算时间',
    pay_channel       varchar(32)  NULL     DEFAULT '' COMMENT '打款渠道',
    pay_reference     varchar(64)  NULL     DEFAULT '' COMMENT '打款流水号',
    remark            varchar(255) NULL     DEFAULT '' COMMENT '备注',
    creator           varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_settlement_no (tenant_id, settlement_no),
    UNIQUE KEY uk_tenant_service_order (tenant_id, service_order_id),
    INDEX idx_tenant_worker (tenant_id, worker_id),
    INDEX idx_tenant_status (tenant_id, settlement_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '打手结算表';

-- 10. 订单日志表
CREATE TABLE IF NOT EXISTS delta_order_log
(
    id               bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    tenant_id        bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    service_order_id bigint       NOT NULL COMMENT '服务订单ID',
    operator_type    tinyint      NOT NULL COMMENT '操作人类型：1-客户 2-打手 3-客服 4-系统',
    operator_id      bigint       NOT NULL COMMENT '操作人ID',
    operation        varchar(64)  NOT NULL COMMENT '操作名称',
    before_status    tinyint      NULL     DEFAULT NULL COMMENT '操作前状态',
    after_status     tinyint      NULL     DEFAULT NULL COMMENT '操作后状态',
    content          varchar(500) NULL     DEFAULT '' COMMENT '日志内容',
    creator          varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    INDEX idx_tenant_service_order_time (tenant_id, service_order_id, create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单日志表';
