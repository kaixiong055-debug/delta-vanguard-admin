-- Delta 先锋俱乐部：页面、菜单、权限补全脚本
-- 适用：当前 system_menu 新版字段结构（name / permission / type / sort / parent_id / ...）
-- 兼容：MySQL 5.7；不使用递归 CTE；不删除任何现有菜单；可重复执行
-- 说明：本脚本只修复菜单、角色菜单关系和启用租户套餐的 menu_ids，不写入业务演示数据。

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_delta_menu_direct (
  name VARCHAR(50) NOT NULL,
  type TINYINT NOT NULL,
  sort_num INT NOT NULL,
  path VARCHAR(200) NOT NULL,
  icon VARCHAR(100) NULL,
  component VARCHAR(255) NULL,
  component_name VARCHAR(255) NULL,
  PRIMARY KEY (path, type)
) ENGINE = MEMORY;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_delta_menu_nested (
  name VARCHAR(50) NOT NULL,
  sort_num INT NOT NULL,
  parent_path VARCHAR(200) NOT NULL,
  path VARCHAR(200) NOT NULL,
  icon VARCHAR(100) NULL,
  component VARCHAR(255) NOT NULL,
  component_name VARCHAR(255) NULL,
  PRIMARY KEY (component)
) ENGINE = MEMORY;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_delta_menu_action (
  name VARCHAR(50) NOT NULL,
  permission VARCHAR(100) NOT NULL,
  sort_num INT NOT NULL,
  parent_component VARCHAR(255) NOT NULL,
  PRIMARY KEY (parent_component, permission)
) ENGINE = MEMORY;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_delta_menu_target_role (
  role_id BIGINT NOT NULL PRIMARY KEY,
  tenant_id BIGINT NOT NULL
) ENGINE = MEMORY;

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_delta_menu_package_missing (
  package_id BIGINT NOT NULL PRIMARY KEY,
  missing_ids TEXT NOT NULL
) ENGINE = MEMORY;

DELETE FROM tmp_delta_menu_direct;
DELETE FROM tmp_delta_menu_nested;
DELETE FROM tmp_delta_menu_action;
DELETE FROM tmp_delta_menu_target_role;
DELETE FROM tmp_delta_menu_package_missing;

INSERT INTO tmp_delta_menu_direct
  (name, type, sort_num, path, icon, component, component_name)
VALUES
  ('服务订单管理', 1, 1,  'service-order',           'ep:list',             NULL,                                 NULL),
  ('派单中心',     2, 2,  'dispatch',                'ep:connection',       'delta/dispatch/index',               'DeltaDispatch'),
  ('订单市场',     1, 3,  'order-market',            'market',              NULL,                                 NULL),
  ('打手管理',     1, 4,  'worker',                  'ep:user',             NULL,                                 NULL),
  ('打手入驻审核', 1, 5,  'worker-application',      'ep:checked',          NULL,                                 NULL),
  ('俱乐部入驻审核', 2, 6, 'club-application',       'form',                'delta/clubApplication/index',        'DeltaClubApplication'),
  ('俱乐部管理',   2, 7,  'club',                    'peoples',             'delta/club/index',                   'DeltaClub'),
  ('结算管理',     1, 8,  'settlement',              'ep:money',            NULL,                                 NULL),
  ('商品服务配置', 1, 9,  'product-service-config',  'ep:setting',          NULL,                                 NULL),
  ('运营统计',     2, 10, 'statistics',              'chart',               'delta/statistics/index',             'DeltaStatistics'),
  ('财务汇总',     2, 11, 'finance',                 'money',               'delta/finance/index',                'DeltaFinance'),
  ('财务对账',     2, 12, 'finance-reconciliation',  'list',                'delta/financeReconciliation/index',  'DeltaFinanceReconciliation'),
  ('取消申请管理', 2, 13, 'order-cancel',            'ep:circle-close',     'delta/order-cancel/index',           'DeltaOrderCancel'),
  ('售后管理',     2, 14, 'after-sale',              'ep:service',          'delta/after-sale/index',             'DeltaAfterSale'),
  ('退款管理',     2, 15, 'refund',                  'ep:money',            'delta/refund/index',                 'DeltaRefund'),
  ('追回任务管理', 2, 16, 'recovery-task',           'ep:refresh-left',     'delta/recovery-task/index',          'DeltaRecoveryTask'),
  ('事件管理',     1, 17, 'event',                   'ep:message-box',      NULL,                                 NULL);

INSERT INTO tmp_delta_menu_nested
  (name, sort_num, parent_path, path, icon, component, component_name)
VALUES
  ('服务订单列表', 1, 'service-order',          'serviceOrder',          'ep:document',         'delta/service-order/index',          'DeltaServiceOrder'),
  ('平台挂牌管理', 1, 'order-market',           'listing',               'list',                'delta/orderMarket/index',            'DeltaOrderMarket'),
  ('可接订单',     2, 'order-market',           'available',             'example',             'delta/orderMarket/available',        'DeltaOrderMarketAvailable'),
  ('打手列表',     1, 'worker',                 'worker',                'ep:avatar',           'delta/worker/index',                 'DeltaWorker'),
  ('入驻审核列表', 1, 'worker-application',     'worker-application',    'ep:document-checked', 'delta/worker-application/index',     'DeltaWorkerApplication'),
  ('结算列表',     1, 'settlement',             'settlement',            'ep:wallet',           'delta/settlement/index',             'DeltaSettlement'),
  ('配置列表',     1, 'product-service-config', 'product-service-config','ep:tools',            'delta/product-service-config/index', 'DeltaProductServiceConfig'),
  ('Outbox 事件',  1, 'event',                  'list',                  'log',                 'delta/eventOutbox/index',            'DeltaEventOutbox');

-- 每项均来自当前 Java Admin Controller 的 @PreAuthorize。
INSERT INTO tmp_delta_menu_action (name, permission, sort_num, parent_component)
VALUES
  ('服务订单查询', 'delta:service-order:query', 1, 'delta/service-order/index'),
  ('确认服务订单', 'delta:service-order:confirm', 2, 'delta/service-order/index'),
  ('服务订单派单', 'delta:service-order:dispatch', 3, 'delta/service-order/index'),
  ('服务订单改派', 'delta:service-order:reassign', 4, 'delta/service-order/index'),
  ('退回接单池', 'delta:service-order:return-pool', 5, 'delta/service-order/index'),
  ('验收服务订单', 'delta:service-order:accept', 6, 'delta/service-order/index'),
  ('要求返工', 'delta:service-order:rework', 7, 'delta/service-order/index'),

  ('派单查询', 'delta:service-order:query', 1, 'delta/dispatch/index'),
  ('派单', 'delta:service-order:dispatch', 2, 'delta/dispatch/index'),
  ('改派', 'delta:service-order:reassign', 3, 'delta/dispatch/index'),
  ('退回接单池', 'delta:service-order:return-pool', 4, 'delta/dispatch/index'),

  ('挂牌查询', 'delta:order-market:query', 1, 'delta/orderMarket/index'),
  ('发布挂牌', 'delta:order-market:publish', 2, 'delta/orderMarket/index'),
  ('撤回挂牌', 'delta:order-market:withdraw', 3, 'delta/orderMarket/index'),
  ('指定俱乐部', 'delta:order-market:assign', 4, 'delta/orderMarket/index'),
  ('抢单', 'delta:order-market:claim', 1, 'delta/orderMarket/available'),
  ('查看已接订单', 'delta:order-market:query', 2, 'delta/orderMarket/available'),

  ('打手查询', 'delta:worker:query', 1, 'delta/worker/index'),
  ('打手编辑', 'delta:worker:update', 2, 'delta/worker/index'),
  ('打手技能', 'delta:worker:update-skill', 3, 'delta/worker/index'),
  ('打手启停', 'delta:worker:update-status', 4, 'delta/worker/index'),
  ('申请查询', 'delta:worker-application:query', 1, 'delta/worker-application/index'),
  ('通过申请', 'delta:worker-application:approve', 2, 'delta/worker-application/index'),
  ('驳回申请', 'delta:worker-application:reject', 3, 'delta/worker-application/index'),

  ('俱乐部申请查询', 'delta:club-application:query', 1, 'delta/clubApplication/index'),
  ('俱乐部申请审核', 'delta:club-application:audit', 2, 'delta/clubApplication/index'),
  ('俱乐部查询', 'delta:club:query', 1, 'delta/club/index'),
  ('俱乐部更新', 'delta:club:update', 2, 'delta/club/index'),

  ('结算查询', 'delta:worker-settlement:query', 1, 'delta/settlement/index'),
  ('生成结算', 'delta:worker-settlement:generate', 2, 'delta/settlement/index'),
  ('审核结算', 'delta:worker-settlement:approve', 3, 'delta/settlement/index'),
  ('驳回结算', 'delta:worker-settlement:reject', 4, 'delta/settlement/index'),
  ('结算打款', 'delta:worker-settlement:pay', 5, 'delta/settlement/index'),

  ('配置查询', 'delta:product-service-config:query', 1, 'delta/product-service-config/index'),
  ('配置创建', 'delta:product-service-config:create', 2, 'delta/product-service-config/index'),
  ('配置更新', 'delta:product-service-config:update', 3, 'delta/product-service-config/index'),
  ('配置删除', 'delta:product-service-config:delete', 4, 'delta/product-service-config/index'),

  ('运营统计查询', 'delta:statistics:query', 1, 'delta/statistics/index'),
  ('财务汇总查询', 'delta:finance:query', 1, 'delta/finance/index'),
  ('对账记录查询', 'delta:finance-reconciliation:query', 1, 'delta/financeReconciliation/index'),
  ('生成对账', 'delta:finance-reconciliation:generate', 2, 'delta/financeReconciliation/index'),
  ('确认对账', 'delta:finance-reconciliation:confirm', 3, 'delta/financeReconciliation/index'),
  ('重试对账', 'delta:finance-reconciliation:retry', 4, 'delta/financeReconciliation/index'),
  ('取消对账', 'delta:finance-reconciliation:cancel', 5, 'delta/financeReconciliation/index'),
  ('导出对账', 'delta:finance-reconciliation:export', 6, 'delta/financeReconciliation/index'),

  ('取消申请查询', 'delta:order-cancel:query', 1, 'delta/order-cancel/index'),
  ('批准取消申请', 'delta:order-cancel:approve', 2, 'delta/order-cancel/index'),
  ('驳回取消申请', 'delta:order-cancel:reject', 3, 'delta/order-cancel/index'),
  ('售后查询', 'delta:after-sale:query', 1, 'delta/after-sale/index'),
  ('受理售后', 'delta:after-sale:accept', 2, 'delta/after-sale/index'),
  ('驳回售后', 'delta:after-sale:reject', 3, 'delta/after-sale/index'),
  ('售后仲裁', 'delta:after-sale:arbitrate', 4, 'delta/after-sale/index'),
  ('关闭售后', 'delta:after-sale:close', 5, 'delta/after-sale/index'),
  ('退款查询', 'delta:refund-record:query', 1, 'delta/refund/index'),
  ('退款处理', 'delta:refund-record:process', 2, 'delta/refund/index'),
  ('退款完成', 'delta:refund-record:complete', 3, 'delta/refund/index'),
  ('退款撤销', 'delta:refund-record:cancel', 4, 'delta/refund/index'),
  ('追回查询', 'delta:fund-recovery:query', 1, 'delta/recovery-task/index'),
  ('生成追回', 'delta:fund-recovery:generate', 2, 'delta/recovery-task/index'),
  ('追回处理', 'delta:fund-recovery:process', 3, 'delta/recovery-task/index'),
  ('取消追回', 'delta:fund-recovery:cancel', 4, 'delta/recovery-task/index'),

  ('事件查询', 'delta:event-outbox:query', 1, 'delta/eventOutbox/index'),
  ('事件重试', 'delta:event-outbox:retry', 2, 'delta/eventOutbox/index'),
  ('事件标记死亡', 'delta:event-outbox:update', 3, 'delta/eventOutbox/index');

START TRANSACTION;

-- 1. 唯一一级菜单。一级 path 必须以 / 开头。
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name,
   status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT
  'Delta 先锋俱乐部', '', 1, 5, 0, '/delta-vanguard', 'ep:management', NULL, NULL,
  0, b'1', b'0', b'1', 'delta_menu_completion', NOW(), 'delta_menu_completion', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE path = '/delta-vanguard' AND parent_id = 0 AND deleted = b'0'
);

SET @delta_root_id := (
  SELECT id FROM system_menu
  WHERE path = '/delta-vanguard' AND parent_id = 0 AND deleted = b'0'
  ORDER BY id LIMIT 1
);

UPDATE system_menu
SET name = 'Delta 先锋俱乐部', type = 1, parent_id = 0, path = '/delta-vanguard',
    status = 0, visible = b'1', updater = 'delta_menu_completion', update_time = NOW()
WHERE id = @delta_root_id;

-- 2. 一级菜单下的模块目录和直接页面。子菜单 path 不以 / 开头。
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name,
   status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT
  d.name, '', d.type, d.sort_num, @delta_root_id, d.path, d.icon, d.component, d.component_name,
  0, b'1', b'0', IF(d.type = 1, b'1', b'0'),
  'delta_menu_completion', NOW(), 'delta_menu_completion', NOW(), b'0'
FROM tmp_delta_menu_direct d
WHERE NOT EXISTS (
  SELECT 1
  FROM system_menu m
  WHERE m.deleted = b'0'
    AND ((d.component IS NOT NULL AND m.component = d.component)
      OR (d.component IS NULL AND m.type = 1 AND m.path = d.path))
);

UPDATE system_menu m
JOIN tmp_delta_menu_direct d
  ON ((d.component IS NOT NULL AND m.component = d.component)
   OR (d.component IS NULL AND m.type = 1 AND m.path = d.path))
SET m.name = d.name,
    m.type = d.type,
    m.sort = d.sort_num,
    m.parent_id = @delta_root_id,
    m.path = d.path,
    m.icon = d.icon,
    m.component = d.component,
    m.component_name = d.component_name,
    m.status = 0,
    m.visible = b'1',
    m.updater = 'delta_menu_completion',
    m.update_time = NOW()
WHERE m.deleted = b'0';

-- 3. 模块目录下的真实页面。
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name,
   status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT
  n.name, '', 2, n.sort_num, p.id, n.path, n.icon, n.component, n.component_name,
  0, b'1', b'0', b'0', 'delta_menu_completion', NOW(), 'delta_menu_completion', NOW(), b'0'
FROM tmp_delta_menu_nested n
JOIN system_menu p
  ON p.parent_id = @delta_root_id
 AND p.path = n.parent_path
 AND p.type = 1
 AND p.deleted = b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu m WHERE m.component = n.component AND m.deleted = b'0'
);

UPDATE system_menu m
JOIN tmp_delta_menu_nested n ON n.component = m.component
JOIN system_menu p
  ON p.parent_id = @delta_root_id
 AND p.path = n.parent_path
 AND p.type = 1
 AND p.deleted = b'0'
SET m.name = n.name,
    m.type = 2,
    m.sort = n.sort_num,
    m.parent_id = p.id,
    m.path = n.path,
    m.icon = n.icon,
    m.component_name = n.component_name,
    m.status = 0,
    m.visible = b'1',
    m.updater = 'delta_menu_completion',
    m.update_time = NOW()
WHERE m.deleted = b'0';

-- 4. 按钮权限。按钮必须 type=3、visible=0、status=0。
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name,
   status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT
  a.name, a.permission, 3, a.sort_num, p.id, '', '', NULL, NULL,
  0, b'0', b'0', b'0', 'delta_menu_completion', NOW(), 'delta_menu_completion', NOW(), b'0'
FROM tmp_delta_menu_action a
JOIN system_menu p ON p.component = a.parent_component AND p.type = 2 AND p.deleted = b'0'
WHERE COALESCE(p.permission, '') <> a.permission
  AND NOT EXISTS (
  SELECT 1
  FROM system_menu m
  WHERE m.parent_id = p.id AND m.permission = a.permission AND m.type = 3 AND m.deleted = b'0'
);

UPDATE system_menu m
JOIN tmp_delta_menu_action a ON a.permission = m.permission
JOIN system_menu p ON p.id = m.parent_id AND p.component = a.parent_component AND p.deleted = b'0'
SET m.name = a.name,
    m.type = 3,
    m.sort = a.sort_num,
    m.path = '',
    m.icon = '',
    m.component = NULL,
    m.component_name = NULL,
    m.status = 0,
    m.visible = b'0',
    m.keep_alive = b'0',
    m.always_show = b'0',
    m.updater = 'delta_menu_completion',
    m.update_time = NOW()
WHERE m.deleted = b'0';

-- 修正历史 Delta 按钮错误显示问题。
UPDATE system_menu
SET type = 3, status = 0, visible = b'0', path = '', component = NULL, component_name = NULL,
    updater = 'delta_menu_completion', update_time = NOW()
WHERE permission LIKE 'delta:%' AND type = 3 AND deleted = b'0';

-- 5. 角色菜单：补给已经拥有任一 Delta 菜单的角色，以及平台 super_admin。
INSERT INTO tmp_delta_menu_target_role (role_id, tenant_id)
SELECT DISTINCT r.id, r.tenant_id
FROM system_role r
WHERE r.deleted = b'0'
  AND (
    r.code = 'super_admin'
    OR EXISTS (
      SELECT 1
      FROM system_role_menu rm
      JOIN system_menu dm ON dm.id = rm.menu_id AND dm.deleted = b'0'
      WHERE rm.role_id = r.id
        AND rm.deleted = b'0'
        AND (dm.permission LIKE 'delta:%' OR dm.component LIKE 'delta/%')
    )
  );

INSERT INTO system_role_menu
  (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
  tr.role_id, m.id, 'delta_menu_completion', NOW(), 'delta_menu_completion', NOW(), b'0', tr.tenant_id
FROM tmp_delta_menu_target_role tr
JOIN system_menu m
  ON m.deleted = b'0'
 AND (m.id = @delta_root_id OR m.parent_id = @delta_root_id
   OR m.component LIKE 'delta/%' OR m.permission LIKE 'delta:%')
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu rm
  WHERE rm.role_id = tr.role_id AND rm.menu_id = m.id AND rm.deleted = b'0'
);

-- 6. 启用租户套餐：只追加缺失的 Delta menu id，保留套餐原有 menu_ids。
-- JSON_MERGE 为 MySQL 5.7 函数；missing_ids 只包含当前套餐尚未拥有的 id，可重复执行。
INSERT INTO tmp_delta_menu_package_missing (package_id, missing_ids)
SELECT tp.id, GROUP_CONCAT(m.id ORDER BY m.id SEPARATOR ',')
FROM system_tenant_package tp
JOIN system_menu m
  ON m.deleted = b'0'
 AND (m.id = @delta_root_id OR m.parent_id = @delta_root_id
   OR m.component LIKE 'delta/%' OR m.permission LIKE 'delta:%')
WHERE tp.deleted = b'0'
  AND tp.status = 0
  AND JSON_VALID(tp.menu_ids) = 1
  AND JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(m.id AS CHAR), '$') = 0
GROUP BY tp.id;

UPDATE system_tenant_package tp
JOIN tmp_delta_menu_package_missing x ON x.package_id = tp.id
SET tp.menu_ids = CAST(
      JSON_MERGE(CAST(tp.menu_ids AS JSON), CAST(CONCAT('[', x.missing_ids, ']') AS JSON))
      AS CHAR
    ),
    tp.updater = 'delta_menu_completion',
    tp.update_time = NOW();

COMMIT;

-- ============================================================
-- 验证查询（预期所有异常计数均为 0）
-- ============================================================

-- 菜单树：一级 + 模块 + 页面/按钮（不使用递归 CTE）。
SELECT
  m.id, m.name, m.permission, m.type, m.sort, m.parent_id,
  m.path, m.component, m.status, m.visible
FROM system_menu m
WHERE m.deleted = b'0'
  AND (m.id = @delta_root_id OR m.parent_id = @delta_root_id
    OR m.component LIKE 'delta/%' OR m.permission LIKE 'delta:%')
ORDER BY
  CASE WHEN m.id = @delta_root_id THEN 0 WHEN m.parent_id = @delta_root_id THEN 1
       WHEN m.type = 2 THEN 2 ELSE 3 END,
  m.parent_id, m.sort, m.id;

SELECT '一级菜单 path 未以 / 开头' AS check_item, COUNT(*) AS error_count
FROM system_menu WHERE id = @delta_root_id AND path NOT LIKE '/%'
UNION ALL
SELECT 'Delta 子菜单 path 错误以 / 开头', COUNT(*)
FROM system_menu WHERE parent_id <> 0 AND component LIKE 'delta/%' AND path LIKE '/%' AND deleted = b'0'
UNION ALL
SELECT 'Delta 页面 component 重复', COUNT(*)
FROM (
  SELECT component FROM system_menu
  WHERE component LIKE 'delta/%' AND deleted = b'0'
  GROUP BY component HAVING COUNT(*) > 1
) duplicate_component
UNION ALL
SELECT 'Delta 按钮仍然可见', COUNT(*)
FROM system_menu WHERE permission LIKE 'delta:%' AND type = 3 AND visible <> b'0' AND deleted = b'0'
UNION ALL
SELECT 'Delta 菜单未启用', COUNT(*)
FROM system_menu
WHERE (id = @delta_root_id OR parent_id = @delta_root_id OR component LIKE 'delta/%' OR permission LIKE 'delta:%')
  AND status <> 0 AND deleted = b'0'
UNION ALL
SELECT 'Delta 页面父菜单为按钮', COUNT(*)
FROM system_menu child
JOIN system_menu parent ON parent.id = child.parent_id AND parent.deleted = b'0'
WHERE child.component LIKE 'delta/%' AND child.deleted = b'0' AND parent.type = 3
UNION ALL
SELECT '目标角色缺少 Delta 菜单', COUNT(*)
FROM tmp_delta_menu_target_role tr
JOIN system_menu m
  ON m.deleted = b'0'
 AND (m.id = @delta_root_id OR m.parent_id = @delta_root_id
   OR m.component LIKE 'delta/%' OR m.permission LIKE 'delta:%')
LEFT JOIN system_role_menu rm
  ON rm.role_id = tr.role_id AND rm.menu_id = m.id AND rm.deleted = b'0'
WHERE rm.id IS NULL
UNION ALL
SELECT '启用租户套餐缺少 Delta 菜单', COUNT(*)
FROM system_tenant_package tp
JOIN system_menu m
  ON m.deleted = b'0'
 AND (m.id = @delta_root_id OR m.parent_id = @delta_root_id
   OR m.component LIKE 'delta/%' OR m.permission LIKE 'delta:%')
WHERE tp.deleted = b'0' AND tp.status = 0
  AND (JSON_VALID(tp.menu_ids) = 0
    OR JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(m.id AS CHAR), '$') = 0);

-- 修复后的预期业务树：
-- Delta 先锋俱乐部
-- ├─ 服务订单管理 ─ 服务订单列表
-- ├─ 派单中心
-- ├─ 订单市场 ─ 平台挂牌管理 / 可接订单
-- ├─ 打手管理 ─ 打手列表
-- ├─ 打手入驻审核 ─ 入驻审核列表
-- ├─ 俱乐部入驻审核
-- ├─ 俱乐部管理
-- ├─ 结算管理 ─ 结算列表
-- ├─ 商品服务配置 ─ 配置列表
-- ├─ 运营统计
-- ├─ 财务汇总
-- ├─ 财务对账
-- ├─ 取消申请管理（代码中额外存在的 Delta 页面）
-- ├─ 售后管理
-- ├─ 退款管理
-- ├─ 追回任务管理
-- └─ 事件管理 ─ Outbox 事件
