-- ============================================================
-- Phase 9: 俱乐部入驻与管理 菜单与权限 SQL
-- 菜单ID从 7080 开始，不与现有菜单冲突
-- 权限标识格式：delta:module:feature:action
-- ============================================================

-- 1. 商户管理 (主菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7080, '商户管理', '', 1, 4, 0, '/delta/merchant', 'shop', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 2. 入驻申请 (子菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7081, '入驻申请', '', 2, 1, 7080, 'club-application', 'form', 'delta/clubApplication/index', 'DeltaClubApplication', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 入驻申请按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7082, '入驻申请查询', 'delta:club-application:query', 3, 1, 7081, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7083, '入驻申请审核', 'delta:club-application:audit', 3, 2, 7081, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 3. 俱乐部管理 (子菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7084, '俱乐部管理', '', 2, 2, 7080, 'club', 'peoples', 'delta/club/index', 'DeltaClub', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 俱乐部管理按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7085, '俱乐部查询', 'delta:club:query', 3, 1, 7084, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7086, '俱乐部更新', 'delta:club:update', 3, 2, 7084, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);
