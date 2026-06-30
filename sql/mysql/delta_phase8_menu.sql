-- ============================================================
-- Phase 8: 运营中心菜单与权限 SQL
-- 菜单ID从 7060 开始，不与现有菜单冲突
-- 权限标识格式：delta:module:feature:action
-- ============================================================

-- 1. 运营统计 (主菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7060, '运营统计', '', 1, 1, 0, '/delta/statistics', 'chart', 'delta/statistics/index', 'DeltaStatistics', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 运营统计按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7061, '运营统计查询', 'delta:statistics:query', 3, 1, 7060, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 2. 财务汇总 (主菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7062, '财务汇总', '', 1, 2, 0, '/delta/finance', 'money', 'delta/finance/index', 'DeltaFinance', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 财务汇总按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7063, '财务汇总查询', 'delta:finance:query', 3, 1, 7062, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 3. 财务对账 (主菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7064, '财务对账', '', 1, 3, 0, '/delta/finance-reconciliation', 'list', 'delta/financeReconciliation/index', 'DeltaFinanceReconciliation', 0, 1, 0, 0, NOW(), NOW(), 0);

-- 财务对账按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7065, '对账记录查询', 'delta:finance-reconciliation:query', 3, 1, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7066, '生成对账', 'delta:finance-reconciliation:generate', 3, 2, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7067, '确认对账', 'delta:finance-reconciliation:confirm', 3, 3, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7068, '重试对账', 'delta:finance-reconciliation:retry', 3, 4, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7069, '取消对账', 'delta:finance-reconciliation:cancel', 3, 5, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `deleted`)
VALUES (7070, '导出对账', 'delta:finance-reconciliation:export', 3, 6, 7064, '', '', '', '', 0, 1, 0, 0, NOW(), NOW(), 0);
