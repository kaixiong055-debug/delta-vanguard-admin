-- =====================================================
-- Delta 先锋俱乐部 Phase 10：多商户第二阶段 - 平台订单市场基础
-- =====================================================

-- =====================================================
-- 平台订单市场挂牌表（平台级资源，不继承租户）
-- =====================================================
CREATE TABLE IF NOT EXISTS delta_order_market_listing
(
    id                      bigint       NOT NULL AUTO_INCREMENT,
    listing_no              varchar(32)  NOT NULL COMMENT '挂牌编号 (DML + yyyyMMddHHmmss + 自增)',
    service_order_id        bigint       NOT NULL COMMENT '服务订单ID (关联 delta_service_order.id)',
    service_order_no        varchar(32)  NOT NULL COMMENT '服务订单号',
    source_tenant_id        bigint       NOT NULL COMMENT '订单来源租户ID',
    service_type            tinyint      NOT NULL COMMENT '服务类型：1-陪玩 2-护航 3-趣味单',
    service_amount          int          NOT NULL COMMENT '服务金额（单位：分）',
    requirement_summary     varchar(2000) DEFAULT '' COMMENT '需求摘要（脱敏后）',
    listing_status          tinyint      NOT NULL DEFAULT 0 COMMENT '挂牌状态：0-可抢 1-已被接单 2-已撤回 3-已过期 4-已关闭',
    publish_time            datetime     NOT NULL COMMENT '发布时间',
    expire_time             datetime     DEFAULT NULL COMMENT '过期时间',
    claimed_club_id         bigint       DEFAULT NULL COMMENT '接单俱乐部档案ID (关联 delta_club_profile.id)',
    claimed_club_tenant_id  bigint       DEFAULT NULL COMMENT '接单俱乐部租户ID',
    claim_time              datetime     DEFAULT NULL COMMENT '接单时间',
    publisher_id            bigint       NOT NULL COMMENT '发布人ID (关联 admin_user.id)',
    withdraw_reason         varchar(500) DEFAULT '' COMMENT '撤回原因',
    remark                  varchar(500) DEFAULT '' COMMENT '备注',
    active_flag             tinyint      NOT NULL DEFAULT 1 COMMENT '活动标记：1-有效 0-无效，与service_order_id组成唯一约束防止并发创建多个有效挂牌',
    version                 int          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    creator                 varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time             datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                 varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time             datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_listing_no (listing_no),
    UNIQUE KEY uk_service_order_active (service_order_id, active_flag) COMMENT '同一服务订单同时只有一个有效挂牌',
    INDEX idx_service_order_id (service_order_id),
    INDEX idx_listing_status (listing_status),
    INDEX idx_claimed_club (claimed_club_id),
    INDEX idx_claimed_club_tenant (claimed_club_tenant_id),
    INDEX idx_publish_time (publish_time),
    INDEX idx_expire_time (expire_time)
) COMMENT '平台订单市场挂牌表';

-- =====================================================
-- 市场操作日志表（平台级资源，不继承租户）
-- =====================================================
CREATE TABLE IF NOT EXISTS delta_order_market_log
(
    id               bigint       NOT NULL AUTO_INCREMENT,
    listing_id       bigint       NOT NULL COMMENT '挂牌ID (关联 delta_order_market_listing.id)',
    service_order_id bigint       NOT NULL COMMENT '服务订单ID',
    operation_type   varchar(32)  NOT NULL COMMENT '操作类型：PUBLISH/CLAIM/ASSIGN/WITHDRAW/EXPIRE/CLOSE/CLAIM_FAILED',
    operator_type    varchar(20)  NOT NULL COMMENT '操作方类型：PLATFORM/CLUB/SYSTEM',
    operator_id      bigint       DEFAULT NULL COMMENT '操作人ID',
    club_id          bigint       DEFAULT NULL COMMENT '操作俱乐部ID',
    club_tenant_id   bigint       DEFAULT NULL COMMENT '操作俱乐部租户ID',
    before_status    tinyint      DEFAULT NULL COMMENT '操作前状态',
    after_status     tinyint      DEFAULT NULL COMMENT '操作后状态',
    success          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败 1-成功',
    failure_reason   varchar(500) DEFAULT '' COMMENT '失败原因',
    remark           varchar(500) DEFAULT '' COMMENT '备注',
    creator          varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    INDEX idx_listing_id (listing_id),
    INDEX idx_service_order_id (service_order_id),
    INDEX idx_operation_type (operation_type)
) COMMENT '市场操作日志表';

-- =====================================================
-- 菜单和权限 SQL
-- =====================================================

-- 权限：订单市场查询
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '订单市场', 0, 9, 'delta-order-market', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'market',
       '订单市场管理',
       'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = 'delta-order-market' AND deleted = 0);

SET @market_parent_id = (SELECT id FROM system_menu WHERE path = 'delta-order-market' AND deleted = 0 LIMIT 1);

-- 平台挂牌管理菜单
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '平台挂牌管理', @market_parent_id, 1, 'listing', 'delta/orderMarket/index', NULL, NULL, 1, 0, 'C', '0', '0',
       'delta:order-market:query', 'list', '平台挂牌管理菜单', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = 'listing' AND parent_id = @market_parent_id AND deleted = 0);

SET @listing_menu_id = (SELECT id FROM system_menu WHERE path = 'listing' AND parent_id = @market_parent_id AND deleted = 0 LIMIT 1);

-- 可接订单菜单
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '可接订单', @market_parent_id, 2, 'available', 'delta/orderMarket/available', NULL, NULL, 1, 0, 'C', '0', '0',
       'delta:order-market:claim', 'example', '俱乐部可接订单菜单', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE path = 'available' AND parent_id = @market_parent_id AND deleted = 0);

SET @available_menu_id = (SELECT id FROM system_menu WHERE path = 'available' AND parent_id = @market_parent_id AND deleted = 0 LIMIT 1);

-- 按钮权限：发布到市场
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '发布挂牌', @listing_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0',
       'delta:order-market:publish', '', '发布挂牌按钮', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE perms = 'delta:order-market:publish' AND parent_id = @listing_menu_id AND deleted = 0);

-- 按钮权限：撤回挂牌
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '撤回挂牌', @listing_menu_id, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0',
       'delta:order-market:withdraw', '', '撤回挂牌按钮', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE perms = 'delta:order-market:withdraw' AND parent_id = @listing_menu_id AND deleted = 0);

-- 按钮权限：指定俱乐部
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '指定俱乐部', @listing_menu_id, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0',
       'delta:order-market:assign', '', '指定俱乐部接单按钮', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE perms = 'delta:order-market:assign' AND parent_id = @listing_menu_id AND deleted = 0);

-- 按钮权限：查看已接订单
INSERT INTO system_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                          menu_type, visible, status, perms, icon, remark, creator, create_time, updater, update_time,
                          deleted)
SELECT '已接订单', @available_menu_id, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0',
       'delta:order-market:query', '', '查看已接订单', 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE perms = 'delta:order-market:query' AND parent_id = @available_menu_id AND deleted = 0);
