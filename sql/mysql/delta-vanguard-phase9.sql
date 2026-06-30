-- =====================================================
-- Delta 先锋俱乐部 Phase 9：多商户化 - 俱乐部入驻与平台管理
-- =====================================================

-- 俱乐部入驻申请表（不继承 tenant_id，申请发生在租户创建前）
CREATE TABLE IF NOT EXISTS delta_club_application
(
    id                  bigint       NOT NULL AUTO_INCREMENT,
    application_no      varchar(32)  NOT NULL COMMENT '申请编号 (DCA + yyyyMMddHHmmss + 自增)',
    applicant_member_id bigint       NOT NULL COMMENT '申请会员用户ID (关联 member_user.id)',
    club_name           varchar(100) NOT NULL COMMENT '俱乐部名称',
    contact_name        varchar(50)  NOT NULL COMMENT '联系人姓名',
    contact_mobile      varchar(20)  NOT NULL COMMENT '联系人手机号',
    contact_wechat      varchar(50)  DEFAULT '' COMMENT '联系人微信',
    description         varchar(2000) DEFAULT '' COMMENT '俱乐部描述',
    logo_url            varchar(500) DEFAULT '' COMMENT 'Logo URL',
    qualification_urls  varchar(3000) DEFAULT '' COMMENT '资质凭证图片URL列表 (JSON数组)',
    application_status  tinyint      NOT NULL DEFAULT 0 COMMENT '申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销',
    reject_reason       varchar(500) DEFAULT '' COMMENT '拒绝原因',
    auditor_id          bigint       DEFAULT NULL COMMENT '审核人ID (关联 admin_user.id)',
    audit_time          datetime     DEFAULT NULL COMMENT '审核时间',
    approved_tenant_id  bigint       DEFAULT NULL COMMENT '审批通过后关联的租户ID',
    remark              varchar(500) DEFAULT '' COMMENT '审核备注',
    version             int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    creator             varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_application_no (application_no),
    INDEX idx_applicant_member (applicant_member_id),
    INDEX idx_status (application_status),
    INDEX idx_auditor (auditor_id)
) COMMENT '俱乐部入驻申请表';

-- 俱乐部档案表
CREATE TABLE IF NOT EXISTS delta_club_profile
(
    id                       bigint       NOT NULL AUTO_INCREMENT,
    tenant_id                bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    club_code                varchar(32)  NOT NULL COMMENT '俱乐部编码（系统唯一）',
    club_name                varchar(100) NOT NULL COMMENT '俱乐部名称',
    owner_member_id          bigint       NOT NULL COMMENT '俱乐部所有者会员用户ID',
    contact_name             varchar(50)  DEFAULT '' COMMENT '联系人姓名',
    contact_mobile           varchar(20)  DEFAULT '' COMMENT '联系人手机号',
    contact_wechat           varchar(50)  DEFAULT '' COMMENT '联系人微信',
    logo_url                 varchar(500) DEFAULT '' COMMENT 'Logo URL',
    description              varchar(2000) DEFAULT '' COMMENT '俱乐部描述',
    business_status          tinyint      NOT NULL DEFAULT 1 COMMENT '经营状态：0-停用 1-启用',
    platform_commission_rate int          NOT NULL DEFAULT 0 COMMENT '平台抽成比例（万分制，如500=5.00%）',
    max_concurrent_orders    int          DEFAULT 100 COMMENT '最大并发订单数',
    application_id           bigint       DEFAULT NULL COMMENT '关联的入驻申请ID',
    remark                   varchar(500) DEFAULT '' COMMENT '备注',
    version                  int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    creator                  varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time              datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                  varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time              datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                  tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_id (tenant_id),
    UNIQUE KEY uk_club_code (club_code),
    UNIQUE KEY uk_application_id (application_id),
    INDEX idx_owner_member (owner_member_id),
    INDEX idx_business_status (business_status)
) COMMENT '俱乐部档案表';

-- 俱乐部服务范围表
CREATE TABLE IF NOT EXISTS delta_club_service_scope
(
    id              bigint      NOT NULL AUTO_INCREMENT,
    tenant_id       bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    club_profile_id bigint      NOT NULL COMMENT '俱乐部档案ID (关联 delta_club_profile.id)',
    service_type    tinyint     NOT NULL COMMENT '服务类型：1-陪玩 2-护航 3-趣味单',
    enabled         tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    remark          varchar(200) DEFAULT '' COMMENT '备注',
    creator         varchar(64) DEFAULT '' COMMENT '创建者',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         varchar(64) DEFAULT '' COMMENT '更新者',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         tinyint     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_profile_type (tenant_id, club_profile_id, service_type, deleted),
    INDEX idx_club_profile (club_profile_id)
) COMMENT '俱乐部服务范围表';

-- 注意：服务类型(service_type) 复用 ServiceTypeEnum：1-陪玩 2-护航 3-趣味单
-- 抽成比例为万分制：1500 = 15.00%
-- delta_club_application 不继承 TenantBaseDO（申请发生在租户建立前）
-- delta_club_profile 和 delta_club_service_scope 继承 TenantBaseDO
