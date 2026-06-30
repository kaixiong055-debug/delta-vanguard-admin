CREATE TABLE IF NOT EXISTS member_user
(
    id          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    nickname    varchar(30)  NOT NULL DEFAULT '' COMMENT '用户昵称',
    name        varchar(30)  NULL COMMENT '真实名字',
    sex         tinyint      NULL COMMENT '性别',
    birthday    datetime     NULL COMMENT '出生日期',
    area_id     int          NULL COMMENT '所在地',
    mark        varchar(255) NULL COMMENT '用户备注',
    point       int                   DEFAULT 0 NULL COMMENT '积分',
    avatar      varchar(255) NOT NULL DEFAULT '' COMMENT '头像',
    status      tinyint      NOT NULL COMMENT '状态',
    mobile      varchar(11)  NOT NULL COMMENT '手机号',
    email       varchar(50)  NULL COMMENT '邮箱',
    password    varchar(100) NOT NULL DEFAULT '' COMMENT '密码',
    register_ip varchar(32)  NOT NULL COMMENT '注册 IP',
    login_ip    varchar(50)  NULL     DEFAULT '' COMMENT '最后登录IP',
    login_date  datetime     NULL     DEFAULT NULL COMMENT '最后登录时间',
    tag_ids     varchar(255) NULL     DEFAULT NULL COMMENT '用户标签编号列表,以逗号分隔',
    level_id    bigint       NULL     DEFAULT NULL COMMENT '等级编号',
    experience  bigint       NULL     DEFAULT NULL COMMENT '经验',
    group_id    bigint       NULL     DEFAULT NULL COMMENT '用户分组编号',
    creator     varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    create_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    update_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id   bigint       NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会员表';

CREATE TABLE IF NOT EXISTS member_address
(
    id             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        bigint(20) NOT NULL COMMENT '用户ID',
    name           varchar(10) NOT NULL COMMENT '收件人姓名',
    mobile         varchar(20) NOT NULL COMMENT '收件人手机',
    area_id        bigint(20) NOT NULL COMMENT '地区ID',
    detail_address varchar(250) NOT NULL COMMENT '详细地址',
    default_status tinyint(1)  NOT NULL COMMENT '是否默认地址 0-否 1-是',
    create_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator        varchar(64) DEFAULT '' COMMENT '创建者',
    update_time    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    updater        varchar(64) DEFAULT '' COMMENT '更新者',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户收件地址';

CREATE TABLE IF NOT EXISTS member_tag
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(32) NOT NULL COMMENT '标签名称',
    creator     varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id   bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会员标签';

CREATE TABLE IF NOT EXISTS member_level
(
    id              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name            varchar(32) NOT NULL COMMENT '等级名称',
    experience      int         NOT NULL COMMENT '升级经验',
    level           int         NOT NULL COMMENT '等级值',
    discount_percent int        NOT NULL COMMENT '折扣比例',
    icon            varchar(255) NOT NULL COMMENT '等级图标',
    background_url  varchar(255) NOT NULL COMMENT '背景图',
    creator         varchar(64) DEFAULT '' COMMENT '创建者',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         varchar(64) DEFAULT '' COMMENT '更新者',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id       bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    status          tinyint     NOT NULL DEFAULT 0 COMMENT '状态 0-禁用 1-启用',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会员等级';

CREATE TABLE IF NOT EXISTS member_group
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(32) NOT NULL COMMENT '分组名称',
    remark      varchar(255) NOT NULL COMMENT '分组备注',
    status      tinyint     NOT NULL DEFAULT 0 COMMENT '状态 0-禁用 1-启用',
    creator     varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id   bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户分组';

CREATE TABLE IF NOT EXISTS member_brokerage_record
(
    id            int         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       bigint      NOT NULL COMMENT '用户ID',
    biz_id        varchar(64) NOT NULL COMMENT '业务编号',
    biz_type      varchar(32) NOT NULL COMMENT '业务类型',
    title         varchar(255) NOT NULL COMMENT '记录标题',
    price         int         NOT NULL COMMENT '佣金金额',
    total_price   int         NOT NULL COMMENT '总金额',
    description   varchar(500) NOT NULL COMMENT '描述',
    status        varchar(32) NOT NULL COMMENT '状态',
    frozen_days   int         NOT NULL COMMENT '冻结天数',
    unfreeze_time varchar(64) COMMENT '解冻时间',
    creator       varchar(64) DEFAULT '' COMMENT '创建者',
    create_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       varchar(64) DEFAULT '' COMMENT '更新者',
    update_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id     bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '佣金记录';