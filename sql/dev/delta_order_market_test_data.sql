-- Delta 订单市场本地开发测试数据
-- MySQL 5.7；只允许在人工确认的本地/开发数据库执行。
-- 默认安全锁为 0：未改为 1 时不会写入任何持久数据。
-- 本脚本不修改 system_menu，不触发支付，不创建 Outbox/通知，不修改已有业务数据。

SET NAMES utf8mb4;
SET @confirm_local_dev := 0;
SET @test_marker := 'TEST_DELTA_ORDER_MARKET_INIT';

SELECT DATABASE() AS current_database,
       @@hostname AS database_server_hostname,
       @confirm_local_dev AS confirm_local_dev,
       '必须确认 JDBC 指向 127.0.0.1/localhost 且数据库为本地开发库后，手工将 @confirm_local_dev 改为 1' AS safety_notice;

-- 必需迁移检查。当前仓库快照缺少三张 delta_club_* 表，执行前必须先在本地应用正式迁移。
SELECT required.table_name AS missing_required_table
FROM (
  SELECT 'system_tenant' table_name UNION ALL
  SELECT 'system_tenant_package' UNION ALL
  SELECT 'system_users' UNION ALL
  SELECT 'member_user' UNION ALL
  SELECT 'product_spu' UNION ALL
  SELECT 'product_sku' UNION ALL
  SELECT 'delta_product_service_config' UNION ALL
  SELECT 'trade_order' UNION ALL
  SELECT 'trade_order_item' UNION ALL
  SELECT 'delta_service_order' UNION ALL
  SELECT 'delta_order_market_listing' UNION ALL
  SELECT 'delta_order_market_log' UNION ALL
  SELECT 'delta_event_outbox' UNION ALL
  SELECT 'delta_member_notification' UNION ALL
  SELECT 'delta_club_application' UNION ALL
  SELECT 'delta_club_profile' UNION ALL
  SELECT 'delta_club_service_scope'
) required
LEFT JOIN information_schema.tables actual
  ON actual.table_schema = DATABASE() AND actual.table_name = required.table_name
WHERE actual.table_name IS NULL;

SET @required_table_count := (
  SELECT COUNT(*)
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name IN (
      'system_tenant','system_tenant_package','system_users','member_user','product_spu','product_sku',
      'delta_product_service_config','trade_order','trade_order_item','delta_service_order',
      'delta_order_market_listing','delta_order_market_log','delta_event_outbox',
      'delta_member_notification',
      'delta_club_application','delta_club_profile','delta_club_service_scope'
    )
);
SET @schema_ready := IF(@required_table_count = 17, 1, 0);

-- 结构差异诊断：预期 uk_service_order_active 存在，且服务单应有 Phase 3 快照字段。
SELECT
  (SELECT COUNT(*) FROM information_schema.statistics
   WHERE table_schema = DATABASE() AND table_name = 'delta_order_market_listing'
     AND index_name = 'uk_service_order_active') AS active_listing_unique_index_count,
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'delta_service_order'
     AND column_name = 'product_pic_url') AS service_order_product_pic_column_count,
  (SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'delta_service_order'
     AND column_name = 'count') AS service_order_count_column_count;

-- 临时存储过程提供异常处理：任一 DML 失败即 ROLLBACK；过程随后自动删除。
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_test_delta_market_init$$
CREATE PROCEDURE proc_test_delta_market_init()
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    GET DIAGNOSTICS CONDITION 1 @init_error_message = MESSAGE_TEXT;
    ROLLBACK;
    SET @init_failed := 1;
  END;
  SET @init_failed := 0;
  SET @init_error_message := NULL;

  IF @confirm_local_dev = 1 AND @schema_ready = 1 THEN
    START TRANSACTION;

-- 1. 动态选择平台租户、平台管理员和一个已有管理账号的目标俱乐部租户。
-- 平台租户口径：有效租户中存在商品、SKU 和启用后台管理员的第一个租户。
SET @platform_tenant_id := (
  SELECT t.id
  FROM system_tenant t
  WHERE t.status = 0 AND t.deleted = b'0' AND t.expire_time > NOW()
    AND EXISTS (SELECT 1 FROM system_users u
                WHERE u.tenant_id = t.id AND u.status = 0 AND u.deleted = b'0')
    AND EXISTS (SELECT 1 FROM product_sku s
                JOIN product_spu p ON p.id = s.spu_id AND p.tenant_id = t.id
                  AND p.status = 1 AND p.deleted = 0
                WHERE s.tenant_id = t.id AND s.deleted = 0
                  AND COALESCE(s.stock, 0) > 0 AND COALESCE(s.price, 0) > 0)
  ORDER BY t.id LIMIT 1
);

SET @platform_admin_id := (
  SELECT u.id FROM system_users u
  WHERE u.tenant_id = @platform_tenant_id AND u.status = 0 AND u.deleted = b'0'
  ORDER BY u.id LIMIT 1
);

-- 不复用已有俱乐部的租户，避免改变真实俱乐部范围或容量。
SET @target_club_tenant_id := COALESCE(
  (SELECT cp.tenant_id
   FROM delta_club_profile cp
   WHERE cp.club_code = 'TEST_CLUB_TARGET' AND cp.creator = @test_marker AND cp.deleted = 0
   ORDER BY cp.id LIMIT 1),
  (SELECT t.id
   FROM system_tenant t
   WHERE t.id <> @platform_tenant_id
     AND t.status = 0 AND t.deleted = b'0' AND t.expire_time > NOW()
     AND EXISTS (SELECT 1 FROM system_users u
                 WHERE u.tenant_id = t.id AND u.status = 0 AND u.deleted = b'0')
     AND NOT EXISTS (SELECT 1 FROM delta_club_profile cp
                     WHERE cp.tenant_id = t.id AND cp.deleted = 0)
   ORDER BY t.id LIMIT 1)
);

SET @target_club_admin_id := (
  SELECT u.id FROM system_users u
  WHERE u.tenant_id = @target_club_tenant_id AND u.status = 0 AND u.deleted = b'0'
  ORDER BY u.id LIMIT 1
);

SET @tenant_package_id := COALESCE(
  (SELECT tp.id FROM system_tenant_package tp WHERE tp.status = 0 AND tp.deleted = b'0' ORDER BY tp.id LIMIT 1),
  (SELECT t.package_id FROM system_tenant t WHERE t.id = @platform_tenant_id)
);

IF @platform_tenant_id IS NULL OR @platform_admin_id IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '缺少同时具备后台管理员、商品和库存 SKU 的平台租户';
END IF;
IF @target_club_tenant_id IS NULL OR @target_club_admin_id IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '缺少可作为目标俱乐部且具备后台管理员的独立租户';
END IF;
IF @tenant_package_id IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '缺少可用 system_tenant_package，无法创建隔离测试租户';
END IF;

-- 容量已满、停用两个隔离租户只用于平台 assign 资格校验，不创建登录账号或密码。
INSERT INTO system_tenant
  (name, contact_user_id, contact_name, contact_mobile, status, websites, package_id,
   expire_time, account_count, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_CAPACITY', NULL, 'TEST_CONTACT', NULL, 0, '', @tenant_package_id,
       DATE_ADD(NOW(), INTERVAL 10 YEAR), 0, @test_marker, NOW(), @test_marker, NOW(), b'0'
WHERE @confirm_local_dev = 1 AND @platform_tenant_id IS NOT NULL AND @tenant_package_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_tenant WHERE name = 'TEST_CLUB_CAPACITY' AND deleted = b'0');

INSERT INTO system_tenant
  (name, contact_user_id, contact_name, contact_mobile, status, websites, package_id,
   expire_time, account_count, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_DISABLED', NULL, 'TEST_CONTACT', NULL, 0, '', @tenant_package_id,
       DATE_ADD(NOW(), INTERVAL 10 YEAR), 0, @test_marker, NOW(), @test_marker, NOW(), b'0'
WHERE @confirm_local_dev = 1 AND @platform_tenant_id IS NOT NULL AND @tenant_package_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_tenant WHERE name = 'TEST_CLUB_DISABLED' AND deleted = b'0');

SET @capacity_tenant_id := (SELECT id FROM system_tenant WHERE name = 'TEST_CLUB_CAPACITY' AND deleted = b'0' ORDER BY id LIMIT 1);
SET @disabled_tenant_id := (SELECT id FROM system_tenant WHERE name = 'TEST_CLUB_DISABLED' AND deleted = b'0' ORDER BY id LIMIT 1);

-- 2. 平台买家与俱乐部 owner member。无手机号、无微信、无可登录密码。
INSERT INTO member_user
  (nickname, mark, status, password, register_ip, register_terminal, avatar,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_DELTA_BUYER', 'TEST_DELTA_ 本地订单市场测试买家', 0, '', '127.0.0.1', 0, '',
       @platform_tenant_id, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @platform_tenant_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM member_user
                  WHERE tenant_id = @platform_tenant_id
                    AND nickname = 'TEST_DELTA_BUYER' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0);

INSERT INTO member_user
  (nickname, mark, status, password, register_ip, register_terminal, avatar,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_OWNER_TARGET', 'TEST_DELTA_ 本地订单市场测试 owner', 0, '', '127.0.0.1', 0, '',
       @target_club_tenant_id, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @target_club_tenant_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM member_user
                  WHERE tenant_id = @target_club_tenant_id
                    AND nickname = 'TEST_CLUB_OWNER_TARGET' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0);

INSERT INTO member_user
  (nickname, mark, status, password, register_ip, register_terminal, avatar,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_OWNER_CAPACITY', 'TEST_DELTA_ 本地订单市场测试 owner', 0, '', '127.0.0.1', 0, '',
       @capacity_tenant_id, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @capacity_tenant_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM member_user
                  WHERE tenant_id = @capacity_tenant_id
                    AND nickname = 'TEST_CLUB_OWNER_CAPACITY' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0);

INSERT INTO member_user
  (nickname, mark, status, password, register_ip, register_terminal, avatar,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_OWNER_DISABLED', 'TEST_DELTA_ 本地订单市场测试 owner', 0, '', '127.0.0.1', 0, '',
       @disabled_tenant_id, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @disabled_tenant_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM member_user
                  WHERE tenant_id = @disabled_tenant_id
                    AND nickname = 'TEST_CLUB_OWNER_DISABLED' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0);

SET @target_owner_member_id := (SELECT id FROM member_user WHERE tenant_id = @target_club_tenant_id AND nickname = 'TEST_CLUB_OWNER_TARGET' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0 ORDER BY id LIMIT 1);
SET @capacity_owner_member_id := (SELECT id FROM member_user WHERE tenant_id = @capacity_tenant_id AND nickname = 'TEST_CLUB_OWNER_CAPACITY' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0 ORDER BY id LIMIT 1);
SET @disabled_owner_member_id := (SELECT id FROM member_user WHERE tenant_id = @disabled_tenant_id AND nickname = 'TEST_CLUB_OWNER_DISABLED' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0 ORDER BY id LIMIT 1);
SET @buyer_member_id := (SELECT id FROM member_user WHERE tenant_id = @platform_tenant_id AND nickname = 'TEST_DELTA_BUYER' AND mark LIKE 'TEST_DELTA_%' AND deleted = 0 ORDER BY id LIMIT 1);

-- 3. 入驻申请与俱乐部档案。
INSERT INTO delta_club_application
  (application_no, applicant_member_id, club_name, contact_name, contact_mobile, contact_wechat,
   description, logo_url, qualification_urls, application_status, reject_reason, auditor_id,
   audit_time, approved_tenant_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_APP_TARGET', @target_owner_member_id, 'TEST_CLUB_TARGET', 'TEST_CONTACT', '00000000000', '',
       'TEST_DELTA_ 本地订单市场测试俱乐部', '', '[]', 1, '', @platform_admin_id,
       NOW(), @target_club_tenant_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @target_owner_member_id IS NOT NULL AND @platform_admin_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_TARGET' AND deleted = 0);

INSERT INTO delta_club_application
  (application_no, applicant_member_id, club_name, contact_name, contact_mobile, contact_wechat,
   description, logo_url, qualification_urls, application_status, reject_reason, auditor_id,
   audit_time, approved_tenant_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_APP_CAPACITY', @capacity_owner_member_id, 'TEST_CLUB_CAPACITY', 'TEST_CONTACT', '00000000000', '',
       'TEST_DELTA_ 容量已满校验俱乐部', '', '[]', 1, '', @platform_admin_id,
       NOW(), @capacity_tenant_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @capacity_owner_member_id IS NOT NULL AND @platform_admin_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_CAPACITY' AND deleted = 0);

INSERT INTO delta_club_application
  (application_no, applicant_member_id, club_name, contact_name, contact_mobile, contact_wechat,
   description, logo_url, qualification_urls, application_status, reject_reason, auditor_id,
   audit_time, approved_tenant_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT 'TEST_CLUB_APP_DISABLED', @disabled_owner_member_id, 'TEST_CLUB_DISABLED', 'TEST_CONTACT', '00000000000', '',
       'TEST_DELTA_ 停用校验俱乐部', '', '[]', 1, '', @platform_admin_id,
       NOW(), @disabled_tenant_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @disabled_owner_member_id IS NOT NULL AND @platform_admin_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_DISABLED' AND deleted = 0);

SET @target_application_id := (SELECT id FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_TARGET' AND deleted = 0 LIMIT 1);
SET @capacity_application_id := (SELECT id FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_CAPACITY' AND deleted = 0 LIMIT 1);
SET @disabled_application_id := (SELECT id FROM delta_club_application WHERE application_no = 'TEST_CLUB_APP_DISABLED' AND deleted = 0 LIMIT 1);

INSERT INTO delta_club_profile
  (tenant_id, club_code, club_name, owner_member_id, contact_name, contact_mobile, contact_wechat,
   logo_url, description, business_status, platform_commission_rate, max_concurrent_orders,
   application_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT @target_club_tenant_id, 'TEST_CLUB_TARGET', 'TEST_CLUB_TARGET', @target_owner_member_id,
       'TEST_CONTACT', '', '', '', 'TEST_DELTA_ 主验证俱乐部', 1, 500, 5,
       @target_application_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @target_application_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE tenant_id = @target_club_tenant_id AND deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE club_code = 'TEST_CLUB_TARGET' AND deleted = 0);

INSERT INTO delta_club_profile
  (tenant_id, club_code, club_name, owner_member_id, contact_name, contact_mobile, contact_wechat,
   logo_url, description, business_status, platform_commission_rate, max_concurrent_orders,
   application_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT @capacity_tenant_id, 'TEST_CLUB_CAPACITY', 'TEST_CLUB_CAPACITY', @capacity_owner_member_id,
       'TEST_CONTACT', '', '', '', 'TEST_DELTA_ 容量已满俱乐部', 1, 500, 1,
       @capacity_application_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @capacity_application_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE tenant_id = @capacity_tenant_id AND deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE club_code = 'TEST_CLUB_CAPACITY' AND deleted = 0);

INSERT INTO delta_club_profile
  (tenant_id, club_code, club_name, owner_member_id, contact_name, contact_mobile, contact_wechat,
   logo_url, description, business_status, platform_commission_rate, max_concurrent_orders,
   application_id, remark, version, creator, create_time, updater, update_time, deleted)
SELECT @disabled_tenant_id, 'TEST_CLUB_DISABLED', 'TEST_CLUB_DISABLED', @disabled_owner_member_id,
       'TEST_CONTACT', '', '', '', 'TEST_DELTA_ 停用俱乐部', 0, 500, 5,
       @disabled_application_id, 'TEST_DELTA_ 可安全回滚', 0, @test_marker, NOW(), @test_marker, NOW(), 0
WHERE @confirm_local_dev = 1 AND @disabled_application_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE tenant_id = @disabled_tenant_id AND deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM delta_club_profile WHERE club_code = 'TEST_CLUB_DISABLED' AND deleted = 0);

SET @target_club_id := (SELECT id FROM delta_club_profile WHERE club_code = 'TEST_CLUB_TARGET' AND deleted = 0 LIMIT 1);
SET @capacity_club_id := (SELECT id FROM delta_club_profile WHERE club_code = 'TEST_CLUB_CAPACITY' AND deleted = 0 LIMIT 1);
SET @disabled_club_id := (SELECT id FROM delta_club_profile WHERE club_code = 'TEST_CLUB_DISABLED' AND deleted = 0 LIMIT 1);

-- 主俱乐部只开放 1/2；合法的 service_type=3 用于验证服务范围不匹配。
INSERT INTO delta_club_service_scope
  (tenant_id, club_profile_id, service_type, enabled, remark, creator, create_time, updater, update_time, deleted)
SELECT cp.tenant_id, cp.id, st.service_type, 1, 'TEST_DELTA_ 可安全回滚', @test_marker, NOW(), @test_marker, NOW(), 0
FROM delta_club_profile cp
JOIN (
  SELECT 'TEST_CLUB_TARGET' club_code, 1 service_type UNION ALL
  SELECT 'TEST_CLUB_TARGET', 2 UNION ALL
  SELECT 'TEST_CLUB_CAPACITY', 1 UNION ALL
  SELECT 'TEST_CLUB_CAPACITY', 2 UNION ALL
  SELECT 'TEST_CLUB_CAPACITY', 3 UNION ALL
  SELECT 'TEST_CLUB_DISABLED', 1 UNION ALL
  SELECT 'TEST_CLUB_DISABLED', 2 UNION ALL
  SELECT 'TEST_CLUB_DISABLED', 3
) st ON st.club_code = cp.club_code
WHERE @confirm_local_dev = 1 AND cp.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM delta_club_service_scope s
    WHERE s.tenant_id = cp.tenant_id AND s.club_profile_id = cp.id
      AND s.service_type = st.service_type AND s.deleted = 0
  );

-- 4. 为三种服务类型选择真实 SKU；优先复用已有启用配置，其次选择尚未配置的库存 SKU。
SET @sku_type_1 := (
  SELECT candidate.sku_id FROM (
    SELECT c.sku_id, 0 priority FROM delta_product_service_config c
    JOIN product_sku existing_sku ON existing_sku.id = c.sku_id
      AND existing_sku.tenant_id = c.tenant_id AND existing_sku.deleted = 0
      AND COALESCE(existing_sku.stock, 0) > 0 AND COALESCE(existing_sku.price, 0) > 0
    JOIN product_spu existing_spu ON existing_spu.id = existing_sku.spu_id
      AND existing_spu.tenant_id = c.tenant_id AND existing_spu.status = 1 AND existing_spu.deleted = 0
    WHERE c.tenant_id = @platform_tenant_id AND c.service_type = 1 AND c.enabled = 1 AND c.deleted = 0
    UNION ALL
    SELECT s.id, 1 FROM product_sku s
    JOIN product_spu p ON p.id = s.spu_id AND p.tenant_id = @platform_tenant_id AND p.status = 1 AND p.deleted = 0
    WHERE s.tenant_id = @platform_tenant_id AND s.deleted = 0
      AND COALESCE(s.stock, 0) > 0 AND COALESCE(s.price, 0) > 0
      AND NOT EXISTS (SELECT 1 FROM delta_product_service_config c WHERE c.tenant_id = @platform_tenant_id AND c.sku_id = s.id AND c.deleted = 0)
  ) candidate ORDER BY candidate.priority, candidate.sku_id LIMIT 1
);
SET @sku_type_2 := (
  SELECT candidate.sku_id FROM (
    SELECT c.sku_id, 0 priority FROM delta_product_service_config c
    JOIN product_sku existing_sku ON existing_sku.id = c.sku_id
      AND existing_sku.tenant_id = c.tenant_id AND existing_sku.deleted = 0
      AND COALESCE(existing_sku.stock, 0) > 0 AND COALESCE(existing_sku.price, 0) > 0
    JOIN product_spu existing_spu ON existing_spu.id = existing_sku.spu_id
      AND existing_spu.tenant_id = c.tenant_id AND existing_spu.status = 1 AND existing_spu.deleted = 0
    WHERE c.tenant_id = @platform_tenant_id AND c.service_type = 2 AND c.enabled = 1 AND c.deleted = 0
    UNION ALL
    SELECT s.id, 1 FROM product_sku s
    JOIN product_spu p ON p.id = s.spu_id AND p.tenant_id = @platform_tenant_id AND p.status = 1 AND p.deleted = 0
    WHERE s.tenant_id = @platform_tenant_id AND s.id <> @sku_type_1
      AND s.deleted = 0 AND COALESCE(s.stock, 0) > 0 AND COALESCE(s.price, 0) > 0
      AND NOT EXISTS (SELECT 1 FROM delta_product_service_config c WHERE c.tenant_id = @platform_tenant_id AND c.sku_id = s.id AND c.deleted = 0)
  ) candidate ORDER BY candidate.priority, candidate.sku_id LIMIT 1
);
SET @sku_type_3 := (
  SELECT candidate.sku_id FROM (
    SELECT c.sku_id, 0 priority FROM delta_product_service_config c
    JOIN product_sku existing_sku ON existing_sku.id = c.sku_id
      AND existing_sku.tenant_id = c.tenant_id AND existing_sku.deleted = 0
      AND COALESCE(existing_sku.stock, 0) > 0 AND COALESCE(existing_sku.price, 0) > 0
    JOIN product_spu existing_spu ON existing_spu.id = existing_sku.spu_id
      AND existing_spu.tenant_id = c.tenant_id AND existing_spu.status = 1 AND existing_spu.deleted = 0
    WHERE c.tenant_id = @platform_tenant_id AND c.service_type = 3 AND c.enabled = 1 AND c.deleted = 0
    UNION ALL
    SELECT s.id, 1 FROM product_sku s
    JOIN product_spu p ON p.id = s.spu_id AND p.tenant_id = @platform_tenant_id AND p.status = 1 AND p.deleted = 0
    WHERE s.tenant_id = @platform_tenant_id AND s.id NOT IN (@sku_type_1, @sku_type_2)
      AND s.deleted = 0 AND COALESCE(s.stock, 0) > 0 AND COALESCE(s.price, 0) > 0
      AND NOT EXISTS (SELECT 1 FROM delta_product_service_config c WHERE c.tenant_id = @platform_tenant_id AND c.sku_id = s.id AND c.deleted = 0)
  ) candidate ORDER BY candidate.priority, candidate.sku_id LIMIT 1
);

IF @sku_type_1 IS NULL OR @sku_type_2 IS NULL OR @sku_type_3 IS NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '缺少三个可用于服务类型 1/2/3 的独立库存 SKU';
END IF;

INSERT INTO delta_product_service_config
  (tenant_id, spu_id, sku_id, service_type, device_type, required_worker_level,
   allow_designated_worker, allow_public_claim, default_dispatch_mode, max_service_hours,
   commission_rate, enabled, creator, create_time, updater, update_time, deleted)
SELECT @platform_tenant_id, s.spu_id, s.id, cfg.service_type, 3, 1, 0, 1, 3, 8,
       1500, 1, @test_marker, NOW(), @test_marker, NOW(), 0
FROM (
  SELECT @sku_type_1 sku_id, 1 service_type UNION ALL
  SELECT @sku_type_2, 2 UNION ALL
  SELECT @sku_type_3, 3
) cfg
JOIN product_sku s ON s.id = cfg.sku_id AND s.tenant_id = @platform_tenant_id AND s.deleted = 0
WHERE @confirm_local_dev = 1 AND cfg.sku_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM delta_product_service_config c
                  WHERE c.tenant_id = @platform_tenant_id AND c.sku_id = cfg.sku_id AND c.deleted = 0);

-- 5. 商城订单。使用真实枚举编码 mock 并标记为已支付，避免任何待支付任务或真实支付调用。
INSERT INTO trade_order
  (no, type, terminal, user_id, user_ip, user_remark, status, product_count, remark,
   comment_status, pay_status, pay_time, total_price, order_price, discount_price, delivery_price,
   adjust_price, pay_price, delivery_type, pay_order_id, pay_channel_code,
   receiver_name, receiver_mobile, receiver_area_id, receiver_detail_address,
   refund_status, refund_price, coupon_id, coupon_price, point_price,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT sc.trade_no, 0, 20, @buyer_member_id, '127.0.0.1', 'TEST_DELTA_ 脱敏测试订单',
       10, 1, 'TEST_DELTA_ 本地订单市场测试', 0, 1, NOW(), sku.price, sku.price, 0, 0, 0, sku.price,
       2, NULL, 'mock', 'TEST_DELTA_USER', '00000000000', 0, 'TEST_DELTA_NO_REAL_ADDRESS',
       0, 0, 0, 0, 0, @test_marker, NOW(), @test_marker, NOW(), 0, CAST(@platform_tenant_id AS CHAR)
FROM (
  SELECT 'TEST_ORDER_AVAILABLE_01' trade_no, 1 service_type UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_02', 2 UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_03', 1 UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_01', 1 UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_02', 2 UNION ALL
  SELECT 'TEST_ORDER_EXPIRED_01', 3 UNION ALL
  SELECT 'TEST_ORDER_SCOPE_MISMATCH', 3 UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_OCCUPIED', 1 UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_TARGET', 1 UNION ALL
  SELECT 'TEST_ORDER_DISABLED_TARGET', 2
) sc
JOIN product_sku sku ON sku.id = CASE sc.service_type WHEN 1 THEN @sku_type_1 WHEN 2 THEN @sku_type_2 ELSE @sku_type_3 END
WHERE @confirm_local_dev = 1
  AND @platform_tenant_id IS NOT NULL AND @buyer_member_id IS NOT NULL
  AND @platform_admin_id IS NOT NULL AND @target_club_id IS NOT NULL
  AND @capacity_club_id IS NOT NULL AND @disabled_club_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM trade_order o WHERE o.no = sc.trade_no AND o.deleted = 0);

INSERT INTO trade_order_item
  (user_id, order_id, cart_id, spu_id, spu_name, sku_id, properties, pic_url, count,
   comment_status, price, discount_price, delivery_price, adjust_price, pay_price,
   coupon_price, point_price, use_point, give_point, vip_price,
   after_sale_id, after_sale_status, creator, create_time, updater, update_time, deleted)
SELECT @buyer_member_id, o.id, NULL, sku.spu_id, spu.name, sku.id, '[]', sku.pic_url, 1,
       0, sku.price, 0, 0, 0, sku.price, 0, 0, 0, 0, 0,
       NULL, 0, @test_marker, NOW(), @test_marker, NOW(), 0
FROM trade_order o
JOIN (
  SELECT 'TEST_ORDER_AVAILABLE_01' trade_no, 1 service_type UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_02', 2 UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_03', 1 UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_01', 1 UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_02', 2 UNION ALL
  SELECT 'TEST_ORDER_EXPIRED_01', 3 UNION ALL
  SELECT 'TEST_ORDER_SCOPE_MISMATCH', 3 UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_OCCUPIED', 1 UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_TARGET', 1 UNION ALL
  SELECT 'TEST_ORDER_DISABLED_TARGET', 2
) sc ON sc.trade_no = o.no
JOIN product_sku sku ON sku.id = CASE sc.service_type WHEN 1 THEN @sku_type_1 WHEN 2 THEN @sku_type_2 ELSE @sku_type_3 END
JOIN product_spu spu ON spu.id = sku.spu_id AND spu.deleted = 0
WHERE @confirm_local_dev = 1 AND o.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM trade_order_item oi WHERE oi.order_id = o.id AND oi.deleted = 0);

-- 6. Delta 服务订单：保持 source tenant，不把 tenant_id 改为俱乐部租户。
INSERT INTO delta_service_order
  (tenant_id, service_order_no, trade_order_id, trade_order_no, trade_order_item_id,
   buyer_user_id, spu_id, sku_id, product_name, sku_name, service_type, device_type,
   service_amount, dispatch_mode, preferred_worker_id, assigned_worker_id, status, version,
   customer_remark, admin_remark, claim_deadline, accepted_at, started_at, submitted_at,
   verified_at, completed_at, cancel_reason, commission_rate, platform_fee, worker_amount,
   creator, create_time, updater, update_time, deleted)
SELECT @platform_tenant_id, sc.service_order_no, o.id, o.no, oi.id,
       @buyer_member_id, oi.spu_id, oi.sku_id, oi.spu_name, oi.properties,
       sc.service_type, 3, oi.pay_price, 3, NULL, NULL, 10, 0,
       sc.summary_text, 'TEST_DELTA_ 本地订单市场测试', DATE_ADD(NOW(), INTERVAL 2 DAY),
       NULL, NULL, NULL, NULL, NULL, '', cfg.commission_rate, 0, 0,
       @test_marker, NOW(), @test_marker, NOW(), 0
FROM (
  SELECT 'TEST_ORDER_AVAILABLE_01' trade_no, 'TEST_DELTA_SO_AVAILABLE_01' service_order_no, 1 service_type, '陪玩目标：测试关卡，设备 PC，预计 2 小时完成' summary_text UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_02', 'TEST_DELTA_SO_AVAILABLE_02', 2, '护航目标：测试段位，设备 PC，今晚 20:00 后' UNION ALL
  SELECT 'TEST_ORDER_AVAILABLE_03', 'TEST_DELTA_SO_AVAILABLE_03', 1, '陪玩目标：测试任务，设备 PC，仅用于本地验证' UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_01', 'TEST_DELTA_SO_CLAIMED_01', 1, '陪玩目标：已接单测试，设备 PC' UNION ALL
  SELECT 'TEST_ORDER_CLAIMED_02', 'TEST_DELTA_SO_CLAIMED_02', 2, '护航目标：已接单测试，设备 PC' UNION ALL
  SELECT 'TEST_ORDER_EXPIRED_01', 'TEST_DELTA_SO_EXPIRED_01', 3, '趣味单目标：过期状态展示测试' UNION ALL
  SELECT 'TEST_ORDER_SCOPE_MISMATCH', 'TEST_DELTA_SO_SCOPE_MISMATCH', 3, '趣味单目标：服务范围不匹配测试' UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_OCCUPIED', 'TEST_DELTA_SO_CAPACITY_OCCUPIED', 1, '陪玩目标：容量占用测试' UNION ALL
  SELECT 'TEST_ORDER_CAPACITY_TARGET', 'TEST_DELTA_SO_CAPACITY_TARGET', 1, '陪玩目标：容量已满错误测试' UNION ALL
  SELECT 'TEST_ORDER_DISABLED_TARGET', 'TEST_DELTA_SO_DISABLED_TARGET', 2, '护航目标：停用俱乐部错误测试'
) sc
JOIN trade_order o ON o.no = sc.trade_no AND o.deleted = 0
JOIN trade_order_item oi ON oi.order_id = o.id AND oi.deleted = 0
JOIN delta_product_service_config cfg
  ON cfg.tenant_id = @platform_tenant_id AND cfg.sku_id = oi.sku_id
 AND cfg.service_type = sc.service_type AND cfg.enabled = 1 AND cfg.deleted = 0
WHERE @confirm_local_dev = 1
  AND NOT EXISTS (SELECT 1 FROM delta_service_order so
                  WHERE so.tenant_id = @platform_tenant_id AND so.service_order_no = sc.service_order_no AND so.deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM delta_service_order so
                  WHERE so.tenant_id = @platform_tenant_id AND so.trade_order_item_id = oi.id AND so.deleted = 0);

-- 7. 十条挂牌：6 AVAILABLE、3 CLAIMED、1 EXPIRED。
INSERT INTO delta_order_market_listing
  (listing_no, service_order_id, service_order_no, source_tenant_id, service_type,
   service_amount, requirement_summary, listing_status, publish_time, expire_time,
   claimed_club_id, claimed_club_tenant_id, claim_time, publisher_id,
   withdraw_reason, remark, active_flag, version,
   creator, create_time, updater, update_time, deleted)
SELECT sc.listing_no, so.id, so.service_order_no, so.tenant_id, so.service_type,
       so.service_amount, sc.summary_text, sc.listing_status,
       DATE_SUB(NOW(), INTERVAL 2 HOUR),
       CASE WHEN sc.listing_status = 3 THEN DATE_SUB(NOW(), INTERVAL 1 HOUR)
            ELSE DATE_ADD(NOW(), INTERVAL 2 DAY) END,
       CASE sc.claimed_kind WHEN 'TARGET' THEN @target_club_id WHEN 'CAPACITY' THEN @capacity_club_id ELSE NULL END,
       CASE sc.claimed_kind WHEN 'TARGET' THEN @target_club_tenant_id WHEN 'CAPACITY' THEN @capacity_tenant_id ELSE NULL END,
       CASE WHEN sc.listing_status = 1 THEN DATE_SUB(NOW(), INTERVAL 1 HOUR) ELSE NULL END,
       @platform_admin_id, '', CONCAT('TEST_DELTA_ ', sc.scenario_name),
       CASE WHEN sc.listing_status = 0 THEN 1 ELSE 0 END,
       CASE WHEN sc.listing_status = 0 THEN 0 ELSE 1 END,
       @test_marker, NOW(), @test_marker, NOW(), 0
FROM (
  SELECT 'TEST_MARKET_AVAILABLE_01' listing_no, 'TEST_DELTA_SO_AVAILABLE_01' service_order_no, 0 listing_status, NULL claimed_kind, '可正常抢单-陪玩' scenario_name, '陪玩目标：测试关卡，设备 PC，预计 2 小时完成' summary_text UNION ALL
  SELECT 'TEST_MARKET_AVAILABLE_02', 'TEST_DELTA_SO_AVAILABLE_02', 0, NULL, '可正常抢单-护航', '护航目标：测试段位，设备 PC，今晚 20:00 后' UNION ALL
  SELECT 'TEST_MARKET_AVAILABLE_03', 'TEST_DELTA_SO_AVAILABLE_03', 0, NULL, '可正常抢单-陪玩', '陪玩目标：测试任务，设备 PC，仅用于本地验证' UNION ALL
  SELECT 'TEST_MARKET_CLAIMED_01', 'TEST_DELTA_SO_CLAIMED_01', 1, 'TARGET', '当前俱乐部已接单', '陪玩目标：已接单测试，设备 PC' UNION ALL
  SELECT 'TEST_MARKET_CLAIMED_02', 'TEST_DELTA_SO_CLAIMED_02', 1, 'TARGET', '当前俱乐部已接单', '护航目标：已接单测试，设备 PC' UNION ALL
  SELECT 'TEST_MARKET_EXPIRED_01', 'TEST_DELTA_SO_EXPIRED_01', 3, NULL, '已过期', '趣味单目标：过期状态展示测试' UNION ALL
  SELECT 'TEST_MARKET_SCOPE_MISMATCH', 'TEST_DELTA_SO_SCOPE_MISMATCH', 0, NULL, '服务范围不匹配', '趣味单目标：服务范围不匹配测试' UNION ALL
  SELECT 'TEST_MARKET_CAPACITY_OCCUPIED', 'TEST_DELTA_SO_CAPACITY_OCCUPIED', 1, 'CAPACITY', '容量占用', '陪玩目标：容量占用测试' UNION ALL
  SELECT 'TEST_MARKET_CAPACITY_TARGET', 'TEST_DELTA_SO_CAPACITY_TARGET', 0, NULL, '容量已满错误目标', '陪玩目标：容量已满错误测试' UNION ALL
  SELECT 'TEST_MARKET_DISABLED_TARGET', 'TEST_DELTA_SO_DISABLED_TARGET', 0, NULL, '停用俱乐部错误目标', '护航目标：停用俱乐部错误测试'
) sc
JOIN delta_service_order so
  ON so.tenant_id = @platform_tenant_id AND so.service_order_no = sc.service_order_no AND so.deleted = 0
WHERE @confirm_local_dev = 1
  AND NOT EXISTS (SELECT 1 FROM delta_order_market_listing l WHERE l.listing_no = sc.listing_no AND l.deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM delta_order_market_listing l WHERE l.service_order_id = so.id AND l.active_flag = 1 AND l.deleted = 0);

-- 8. 日志：模拟 Service 的 PUBLISH/CLAIM/EXPIRE 状态轨迹；不伪造 Outbox 成功。
INSERT INTO delta_order_market_log
  (listing_id, service_order_id, operation_type, operator_type, operator_id,
   club_id, club_tenant_id, before_status, after_status, success, failure_reason, remark,
   creator, create_time, updater, update_time, deleted)
SELECT l.id, l.service_order_id, 'PUBLISH', 'PLATFORM', @platform_admin_id,
       NULL, NULL, NULL, NULL, 1, '', 'TEST_DELTA_ 直接 SQL 初始化发布日志',
       @test_marker, l.publish_time, @test_marker, l.publish_time, 0
FROM delta_order_market_listing l
WHERE @confirm_local_dev = 1 AND l.listing_no LIKE 'TEST_MARKET_%' AND l.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM delta_order_market_log ml
                  WHERE ml.listing_id = l.id AND ml.operation_type = 'PUBLISH' AND ml.creator = @test_marker AND ml.deleted = 0);

INSERT INTO delta_order_market_log
  (listing_id, service_order_id, operation_type, operator_type, operator_id,
   club_id, club_tenant_id, before_status, after_status, success, failure_reason, remark,
   creator, create_time, updater, update_time, deleted)
SELECT l.id, l.service_order_id, 'CLAIM', 'CLUB', NULL,
       l.claimed_club_id, l.claimed_club_tenant_id, 0, 1, 1, '', 'TEST_DELTA_ 直接 SQL 初始化接单日志',
       @test_marker, l.claim_time, @test_marker, l.claim_time, 0
FROM delta_order_market_listing l
WHERE @confirm_local_dev = 1 AND l.listing_status = 1
  AND l.listing_no IN ('TEST_MARKET_CLAIMED_01','TEST_MARKET_CLAIMED_02','TEST_MARKET_CAPACITY_OCCUPIED')
  AND l.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM delta_order_market_log ml
                  WHERE ml.listing_id = l.id AND ml.operation_type = 'CLAIM' AND ml.creator = @test_marker AND ml.deleted = 0);

INSERT INTO delta_order_market_log
  (listing_id, service_order_id, operation_type, operator_type, operator_id,
   club_id, club_tenant_id, before_status, after_status, success, failure_reason, remark,
   creator, create_time, updater, update_time, deleted)
SELECT l.id, l.service_order_id, 'EXPIRE', 'SYSTEM', NULL,
       NULL, NULL, 0, 3, 1, '', 'TEST_DELTA_ 直接 SQL 初始化过期日志',
       @test_marker, l.expire_time, @test_marker, l.expire_time, 0
FROM delta_order_market_listing l
WHERE @confirm_local_dev = 1 AND l.listing_no = 'TEST_MARKET_EXPIRED_01' AND l.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM delta_order_market_log ml
                  WHERE ml.listing_id = l.id AND ml.operation_type = 'EXPIRE' AND ml.creator = @test_marker AND ml.deleted = 0);

    COMMIT;
  END IF;
END$$
CALL proc_test_delta_market_init()$$
DROP PROCEDURE IF EXISTS proc_test_delta_market_init$$
DELIMITER ;

SELECT @schema_ready AS schema_ready,
       @init_failed AS init_failed,
       @init_error_message AS init_error_message,
       IF(@confirm_local_dev = 0, '安全锁未开启，未写入',
          IF(@schema_ready = 0, '缺少正式迁移表，未写入',
             IF(@init_failed = 1, '执行失败并已回滚', '执行成功'))) AS execution_result;

-- ============================================================
-- 验证查询
-- ============================================================
SELECT @platform_tenant_id AS platform_tenant_id,
       @platform_admin_id AS platform_admin_id,
       @target_club_tenant_id AS target_club_tenant_id,
       @target_club_admin_id AS target_club_admin_id,
       @buyer_member_id AS buyer_member_id,
       @target_owner_member_id AS target_owner_member_id,
       @target_club_id AS target_club_id,
       @capacity_tenant_id AS capacity_tenant_id,
       @capacity_club_id AS capacity_club_id,
       @disabled_tenant_id AS disabled_tenant_id,
       @disabled_club_id AS disabled_club_id;

SELECT 'club_application' table_name, COUNT(*) row_count FROM delta_club_application WHERE application_no LIKE 'TEST_CLUB_APP_%' AND deleted = 0
UNION ALL SELECT 'club_profile', COUNT(*) FROM delta_club_profile WHERE club_code LIKE 'TEST_CLUB_%' AND deleted = 0
UNION ALL SELECT 'club_service_scope', COUNT(*) FROM delta_club_service_scope WHERE remark LIKE 'TEST_DELTA_%' AND deleted = 0
UNION ALL SELECT 'trade_order', COUNT(*) FROM trade_order WHERE no LIKE 'TEST_ORDER_%' AND deleted = 0
UNION ALL SELECT 'trade_order_item', COUNT(*) FROM trade_order_item oi JOIN trade_order o ON o.id = oi.order_id WHERE o.no LIKE 'TEST_ORDER_%' AND o.deleted = 0 AND oi.deleted = 0
UNION ALL SELECT 'delta_service_order', COUNT(*) FROM delta_service_order WHERE service_order_no LIKE 'TEST_DELTA_SO_%' AND deleted = 0
UNION ALL SELECT 'market_listing', COUNT(*) FROM delta_order_market_listing WHERE listing_no LIKE 'TEST_MARKET_%' AND deleted = 0
UNION ALL SELECT 'market_log', COUNT(*) FROM delta_order_market_log ml JOIN delta_order_market_listing l ON l.id = ml.listing_id WHERE l.listing_no LIKE 'TEST_MARKET_%' AND l.deleted = 0 AND ml.deleted = 0
UNION ALL SELECT 'outbox intentionally omitted', COUNT(*) FROM delta_event_outbox WHERE biz_key LIKE 'market:TEST_MARKET_%' AND deleted = 0;

SELECT listing_status, COUNT(*) row_count
FROM delta_order_market_listing
WHERE listing_no LIKE 'TEST_MARKET_%' AND deleted = 0
GROUP BY listing_status ORDER BY listing_status;

-- 平台挂牌分页/详情等价数据。
SELECT l.id, l.listing_no, l.service_order_no, l.source_tenant_id, l.service_type,
       l.service_amount, l.listing_status, l.expire_time,
       l.claimed_club_id, l.claimed_club_tenant_id, l.active_flag, l.version
FROM delta_order_market_listing l
WHERE l.listing_no LIKE 'TEST_MARKET_%' AND l.deleted = 0
ORDER BY l.id DESC;

-- 目标俱乐部可接订单分页等价查询：只应返回 AVAILABLE、未过期、service_type 1/2；type 3 mismatch 不应出现。
SELECT l.id, l.listing_no, l.service_type, l.listing_status, l.expire_time
FROM delta_order_market_listing l
WHERE l.listing_status = 0 AND (l.expire_time IS NULL OR l.expire_time > NOW()) AND l.deleted = 0
  AND l.service_type IN (
    SELECT s.service_type FROM delta_club_service_scope s
    WHERE s.club_profile_id = @target_club_id AND s.enabled = 1 AND s.deleted = 0
  )
  AND l.listing_no LIKE 'TEST_MARKET_%'
ORDER BY l.id;

-- 我的已接订单分页等价查询。
SELECT l.id, l.listing_no, l.claimed_club_id, l.claimed_club_tenant_id, l.claim_time
FROM delta_order_market_listing l
WHERE l.claimed_club_tenant_id = @target_club_tenant_id AND l.listing_status = 1 AND l.deleted = 0
ORDER BY l.id DESC;

-- 容量与停用校验数据。
SELECT cp.id club_id, cp.tenant_id, cp.club_code, cp.business_status, cp.max_concurrent_orders,
       SUM(CASE WHEN l.listing_status = 1 AND so.status NOT IN (80,90,100,110) THEN 1 ELSE 0 END) non_terminal_claimed_count
FROM delta_club_profile cp
LEFT JOIN delta_order_market_listing l ON l.claimed_club_tenant_id = cp.tenant_id AND l.deleted = 0
LEFT JOIN delta_service_order so ON so.id = l.service_order_id AND so.deleted = 0
WHERE cp.club_code IN ('TEST_CLUB_CAPACITY','TEST_CLUB_DISABLED') AND cp.deleted = 0
GROUP BY cp.id, cp.tenant_id, cp.club_code, cp.business_status, cp.max_concurrent_orders;

-- 日志查询等价数据。
SELECT ml.listing_id, l.listing_no, ml.operation_type, ml.operator_type,
       ml.club_id, ml.club_tenant_id, ml.before_status, ml.after_status, ml.success, ml.create_time
FROM delta_order_market_log ml
JOIN delta_order_market_listing l ON l.id = ml.listing_id
WHERE l.listing_no LIKE 'TEST_MARKET_%' AND l.deleted = 0 AND ml.deleted = 0
ORDER BY ml.listing_id, ml.id;

-- 孤儿检查，预期均为 0。
SELECT 'listing_without_service_order' check_item, COUNT(*) error_count
FROM delta_order_market_listing l LEFT JOIN delta_service_order so ON so.id = l.service_order_id AND so.deleted = 0
WHERE l.listing_no LIKE 'TEST_MARKET_%' AND l.deleted = 0 AND so.id IS NULL
UNION ALL
SELECT 'service_order_without_trade_order', COUNT(*)
FROM delta_service_order so LEFT JOIN trade_order o ON o.id = so.trade_order_id AND o.deleted = 0
WHERE so.service_order_no LIKE 'TEST_DELTA_SO_%' AND so.deleted = 0 AND o.id IS NULL
UNION ALL
SELECT 'service_order_without_trade_item', COUNT(*)
FROM delta_service_order so LEFT JOIN trade_order_item oi ON oi.id = so.trade_order_item_id AND oi.deleted = 0
WHERE so.service_order_no LIKE 'TEST_DELTA_SO_%' AND so.deleted = 0 AND oi.id IS NULL
UNION ALL
SELECT 'service_order_without_member', COUNT(*)
FROM delta_service_order so LEFT JOIN member_user mu ON mu.id = so.buyer_user_id AND mu.deleted = 0
WHERE so.service_order_no LIKE 'TEST_DELTA_SO_%' AND so.deleted = 0 AND mu.id IS NULL
UNION ALL
SELECT 'claimed_listing_without_club', COUNT(*)
FROM delta_order_market_listing l LEFT JOIN delta_club_profile cp ON cp.id = l.claimed_club_id AND cp.deleted = 0
WHERE l.listing_no LIKE 'TEST_MARKET_%' AND l.listing_status = 1 AND l.deleted = 0 AND cp.id IS NULL;
