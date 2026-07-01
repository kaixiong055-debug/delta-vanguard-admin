-- Delta 俱乐部入驻 + 平台订单市场审核用测试数据
-- MySQL 5.7；默认安全锁关闭，只生成文件，不自动执行。
-- 目标：1 待审核申请、1 已通过申请、1 启用俱乐部、3 个服务范围、3 可抢挂牌、1 已接挂牌。

SET NAMES utf8mb4;
SET @confirm_local_dev := 0;
SET @test_marker := 'TEST_DELTA_CLUB_MARKET_REVIEW';

-- 快照基础数据：admin_portal 2026-06-30。
SET @platform_tenant_id := 1;
SET @club_tenant_id := 121;
SET @approved_member_id := 1;
SET @pending_member_id := 2;
SET @sku_service_1 := 2;
SET @sku_service_2 := 3;
SET @sku_service_3 := 4;

SELECT DATABASE() AS current_database,
       @@hostname AS database_server_hostname,
       @confirm_local_dev AS confirm_local_dev,
       '确认 JDBC 为本地/开发库后，才可手工改为 1' AS safety_notice;

-- 目标表最小存在性检查。
SELECT required.table_name AS missing_required_table
FROM (
  SELECT 'system_tenant' table_name UNION ALL
  SELECT 'system_users' UNION ALL
  SELECT 'member_user' UNION ALL
  SELECT 'product_spu' UNION ALL
  SELECT 'product_sku' UNION ALL
  SELECT 'delta_product_service_config' UNION ALL
  SELECT 'trade_order' UNION ALL
  SELECT 'trade_order_item' UNION ALL
  SELECT 'delta_club_application' UNION ALL
  SELECT 'delta_club_profile' UNION ALL
  SELECT 'delta_club_service_scope' UNION ALL
  SELECT 'delta_service_order' UNION ALL
  SELECT 'delta_order_market_listing' UNION ALL
  SELECT 'delta_order_market_log' UNION ALL
  SELECT 'delta_event_outbox' UNION ALL
  SELECT 'delta_member_notification'
) required
LEFT JOIN information_schema.tables actual
  ON actual.table_schema = DATABASE() AND actual.table_name = required.table_name
WHERE actual.table_name IS NULL;

SET @required_table_count := (
  SELECT COUNT(*) FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name IN (
      'system_tenant','system_users','member_user','product_spu','product_sku',
      'delta_product_service_config','trade_order','trade_order_item',
      'delta_club_application','delta_club_profile','delta_club_service_scope',
      'delta_service_order','delta_order_market_listing','delta_order_market_log',
      'delta_event_outbox','delta_member_notification'
    )
);
SET @active_index_count := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'delta_order_market_listing'
    AND index_name = 'uk_service_order_active'
);
SET @service_snapshot_column_count := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'delta_service_order'
    AND column_name IN ('product_pic_url', 'count')
);
SET @schema_ready := IF(
  @required_table_count = 16
  AND @active_index_count > 0
  AND @service_snapshot_column_count = 2,
  1, 0
);

SELECT @required_table_count AS required_table_count,
       @active_index_count AS active_listing_unique_index_count,
       @service_snapshot_column_count AS service_snapshot_column_count,
       @schema_ready AS schema_ready;

DELIMITER $$
DROP PROCEDURE IF EXISTS proc_test_delta_club_market_review_init$$
CREATE PROCEDURE proc_test_delta_club_market_review_init()
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

    SET @platform_admin_id := (
      SELECT id FROM system_users
      WHERE tenant_id = @platform_tenant_id AND status = 0 AND deleted = b'0'
      ORDER BY id LIMIT 1
    );

    IF NOT EXISTS (
      SELECT 1 FROM system_tenant
      WHERE id = @platform_tenant_id AND status = 0 AND deleted = b'0' AND expire_time > NOW()
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '平台 tenantId=1 不存在、已停用或已过期';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM system_tenant
      WHERE id = @club_tenant_id AND status = 0 AND deleted = b'0' AND expire_time > NOW()
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '俱乐部 tenantId=121 不存在、已停用或已过期';
    END IF;
    IF @platform_admin_id IS NULL THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '平台租户缺少可用后台管理员';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM system_users
      WHERE tenant_id = @club_tenant_id AND status = 0 AND deleted = b'0'
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '俱乐部租户缺少可用后台账号，无法验证可接订单';
    END IF;
    IF (SELECT COUNT(*) FROM member_user
        WHERE id IN (@approved_member_id, @pending_member_id)
          AND tenant_id = @platform_tenant_id AND status = 0 AND deleted = 0) <> 2 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '快照会员 member_user 1/2 不可用';
    END IF;
    IF EXISTS (
      SELECT 1 FROM delta_club_profile
      WHERE tenant_id = @club_tenant_id AND deleted = 0
        AND club_code <> 'TEST_CLUB_MARKET_REVIEW'
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'tenantId=121 已存在非本脚本俱乐部档案';
    END IF;
    IF EXISTS (
      SELECT 1 FROM delta_club_application
      WHERE applicant_member_id = @pending_member_id
        AND application_status = 0 AND deleted = 0
        AND application_no <> 'TEST_CLUB_APP_PENDING'
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '待审核测试会员已有其他待审核入驻申请';
    END IF;
    IF EXISTS (
      SELECT 1 FROM delta_club_profile
      WHERE owner_member_id = @approved_member_id AND deleted = 0
        AND club_code <> 'TEST_CLUB_MARKET_REVIEW'
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '已通过测试会员已经是其他俱乐部 owner';
    END IF;

    -- 三个 SKU 必须属于上架 SPU，并有库存和正金额。
    IF (SELECT COUNT(*)
        FROM product_sku sku
        JOIN product_spu spu ON spu.id = sku.spu_id
          AND spu.tenant_id = @platform_tenant_id AND spu.status = 1 AND spu.deleted = 0
        WHERE sku.id IN (@sku_service_1, @sku_service_2, @sku_service_3)
          AND sku.tenant_id = @platform_tenant_id AND sku.deleted = 0
          AND sku.stock > 0 AND sku.price > 0) <> 3 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '快照 SKU 2/3/4 不满足上架、库存或金额要求';
    END IF;
    IF EXISTS (
      SELECT 1 FROM delta_product_service_config
      WHERE tenant_id = @platform_tenant_id AND deleted = 0
        AND ((sku_id = @sku_service_1 AND service_type <> 1)
          OR (sku_id = @sku_service_2 AND service_type <> 2)
          OR (sku_id = @sku_service_3 AND service_type <> 3)
          OR (sku_id IN (@sku_service_1, @sku_service_2, @sku_service_3) AND enabled <> 1))
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SKU 已存在不同 serviceType 的服务配置';
    END IF;

    INSERT INTO delta_product_service_config
      (tenant_id, spu_id, sku_id, service_type, device_type, required_worker_level,
       allow_designated_worker, allow_public_claim, default_dispatch_mode,
       max_service_hours, commission_rate, enabled,
       creator, create_time, updater, update_time, deleted)
    SELECT @platform_tenant_id, sku.spu_id, sku.id, mapping.service_type,
           3, 1, 0, 1, 3, 8, 1500, 1,
           @test_marker, NOW(), @test_marker, NOW(), 0
    FROM (
      SELECT @sku_service_1 sku_id, 1 service_type UNION ALL
      SELECT @sku_service_2, 2 UNION ALL
      SELECT @sku_service_3, 3
    ) mapping
    JOIN product_sku sku ON sku.id = mapping.sku_id AND sku.deleted = 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_product_service_config config
      WHERE config.tenant_id = @platform_tenant_id
        AND config.sku_id = mapping.sku_id AND config.deleted = 0
    );

    -- 待审核申请：无审核人、审核时间和 approvedTenantId。
    INSERT INTO delta_club_application
      (application_no, applicant_member_id, club_name, contact_name, contact_mobile,
       contact_wechat, description, logo_url, qualification_urls,
       application_status, reject_reason, auditor_id, audit_time, approved_tenant_id,
       remark, version, creator, create_time, updater, update_time, deleted)
    SELECT 'TEST_CLUB_APP_PENDING', @pending_member_id, 'TEST_CLUB_PENDING',
           '测试联系人', '00000000000', '', 'TEST_DELTA_ 待审核入驻申请', '', '[]',
           0, '', NULL, NULL, NULL, 'TEST_DELTA_ 可安全回滚', 0,
           @test_marker, NOW(), @test_marker, NOW(), 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_club_application
      WHERE application_no = 'TEST_CLUB_APP_PENDING' AND deleted = 0
    );

    -- 已通过申请：状态与真实审核 Service 完成后的 version=1 一致。
    INSERT INTO delta_club_application
      (application_no, applicant_member_id, club_name, contact_name, contact_mobile,
       contact_wechat, description, logo_url, qualification_urls,
       application_status, reject_reason, auditor_id, audit_time, approved_tenant_id,
       remark, version, creator, create_time, updater, update_time, deleted)
    SELECT 'TEST_CLUB_APP_APPROVED', @approved_member_id, 'TEST_CLUB_MARKET_REVIEW',
           '测试联系人', '00000000000', '', 'TEST_DELTA_ 已通过入驻申请', '', '[]',
           1, '', @platform_admin_id, NOW(), @club_tenant_id,
           'TEST_DELTA_ 可安全回滚', 1,
           @test_marker, NOW(), @test_marker, NOW(), 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_club_application
      WHERE application_no = 'TEST_CLUB_APP_APPROVED' AND deleted = 0
    );

    SET @approved_application_id := (
      SELECT id FROM delta_club_application
      WHERE application_no = 'TEST_CLUB_APP_APPROVED' AND deleted = 0 LIMIT 1
    );

    INSERT INTO delta_club_profile
      (tenant_id, club_code, club_name, owner_member_id,
       contact_name, contact_mobile, contact_wechat, logo_url, description,
       business_status, platform_commission_rate, max_concurrent_orders,
       application_id, remark, version,
       creator, create_time, updater, update_time, deleted)
    SELECT @club_tenant_id, 'TEST_CLUB_MARKET_REVIEW', 'TEST_CLUB_MARKET_REVIEW',
           @approved_member_id, '测试联系人', '', '', '', 'TEST_DELTA_ 订单市场审核俱乐部',
           1, 1500, 10, @approved_application_id, 'TEST_DELTA_ 可安全回滚', 0,
           @test_marker, NOW(), @test_marker, NOW(), 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_club_profile
      WHERE tenant_id = @club_tenant_id AND deleted = 0
    )
      AND NOT EXISTS (
        SELECT 1 FROM delta_club_profile
        WHERE club_code = 'TEST_CLUB_MARKET_REVIEW' AND deleted = 0
      );

    SET @club_id := (
      SELECT id FROM delta_club_profile
      WHERE tenant_id = @club_tenant_id
        AND club_code = 'TEST_CLUB_MARKET_REVIEW' AND deleted = 0 LIMIT 1
    );

    INSERT INTO delta_club_service_scope
      (tenant_id, club_profile_id, service_type, enabled, remark,
       creator, create_time, updater, update_time, deleted)
    SELECT @club_tenant_id, @club_id, service_type, 1, 'TEST_DELTA_ 可安全回滚',
           @test_marker, NOW(), @test_marker, NOW(), 0
    FROM (
      SELECT 1 service_type UNION ALL SELECT 2 UNION ALL SELECT 3
    ) service_types
    WHERE @club_id IS NOT NULL
      AND NOT EXISTS (
        SELECT 1 FROM delta_club_service_scope scope
        WHERE scope.tenant_id = @club_tenant_id
          AND scope.club_profile_id = @club_id
          AND scope.service_type = service_types.service_type
          AND scope.deleted = 0
      );

    -- 四条完整商城订单：使用 mock 渠道，已支付但没有真实 pay_order_id。
    INSERT INTO trade_order
      (no, type, terminal, user_id, user_ip, user_remark, status, product_count,
       remark, comment_status, pay_status, pay_time,
       total_price, order_price, discount_price, delivery_price, adjust_price, pay_price,
       delivery_type, pay_order_id, pay_channel_code,
       receiver_name, receiver_mobile, receiver_area_id, receiver_detail_address,
       refund_status, refund_price, coupon_id, coupon_price, point_price,
       creator, create_time, updater, update_time, deleted, tenant_id)
    SELECT scenario.trade_no, 0, 20, @approved_member_id, '127.0.0.1',
           'TEST_DELTA_ 脱敏订单市场审核数据', 10, 1, 'TEST_DELTA_ 可安全回滚',
           0, 1, NOW(), sku.price, sku.price, 0, 0, 0, sku.price,
           2, NULL, 'mock', '测试用户', '00000000000', 0, 'TEST_DELTA_NO_REAL_ADDRESS',
           0, 0, 0, 0, 0,
           @test_marker, NOW(), @test_marker, NOW(), 0, CAST(@platform_tenant_id AS CHAR)
    FROM (
      SELECT 'TEST_ORDER_CM_AVAIL_01' trade_no, @sku_service_1 sku_id UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_02', @sku_service_2 UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_03', @sku_service_3 UNION ALL
      SELECT 'TEST_ORDER_CM_CLAIMED_01', @sku_service_1
    ) scenario
    JOIN product_sku sku ON sku.id = scenario.sku_id AND sku.deleted = 0
    WHERE NOT EXISTS (
      SELECT 1 FROM trade_order existing_order
      WHERE existing_order.no = scenario.trade_no AND existing_order.deleted = 0
    );

    INSERT INTO trade_order_item
      (user_id, order_id, cart_id, spu_id, spu_name, sku_id, properties, pic_url, count,
       comment_status, price, discount_price, delivery_price, adjust_price, pay_price,
       coupon_price, point_price, use_point, give_point, vip_price,
       after_sale_id, after_sale_status,
       creator, create_time, updater, update_time, deleted)
    SELECT @approved_member_id, trade.id, NULL, sku.spu_id, spu.name, sku.id,
           '[]', sku.pic_url, 1, 0, sku.price, 0, 0, 0, sku.price,
           0, 0, 0, 0, 0, NULL, 0,
           @test_marker, NOW(), @test_marker, NOW(), 0
    FROM trade_order trade
    JOIN (
      SELECT 'TEST_ORDER_CM_AVAIL_01' trade_no, @sku_service_1 sku_id UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_02', @sku_service_2 UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_03', @sku_service_3 UNION ALL
      SELECT 'TEST_ORDER_CM_CLAIMED_01', @sku_service_1
    ) scenario ON scenario.trade_no = trade.no
    JOIN product_sku sku ON sku.id = scenario.sku_id AND sku.deleted = 0
    JOIN product_spu spu ON spu.id = sku.spu_id AND spu.deleted = 0
    WHERE trade.deleted = 0
      AND NOT EXISTS (
        SELECT 1 FROM trade_order_item item
        WHERE item.order_id = trade.id AND item.deleted = 0
      );

    INSERT INTO delta_service_order
      (tenant_id, service_order_no, trade_order_id, trade_order_no, trade_order_item_id,
       buyer_user_id, spu_id, sku_id, product_name, sku_name, product_pic_url, count,
       service_type, device_type, service_amount, dispatch_mode,
       preferred_worker_id, assigned_worker_id, status, version,
       customer_remark, admin_remark, claim_deadline,
       accepted_at, started_at, submitted_at, verified_at, completed_at,
       cancel_reason, commission_rate, platform_fee, worker_amount,
       creator, create_time, updater, update_time, deleted)
    SELECT @platform_tenant_id, scenario.service_order_no,
           trade.id, trade.no, item.id, @approved_member_id,
           item.spu_id, item.sku_id, item.spu_name, item.properties, item.pic_url, item.count,
           scenario.service_type, 3, item.pay_price, 3,
           NULL, NULL, 10, 0,
           scenario.summary_text, 'TEST_DELTA_ 可安全回滚', DATE_ADD(NOW(), INTERVAL 2 DAY),
           NULL, NULL, NULL, NULL, NULL, '', config.commission_rate, 0, 0,
           @test_marker, NOW(), @test_marker, NOW(), 0
    FROM (
      SELECT 'TEST_ORDER_CM_AVAIL_01' trade_no,
             'TEST_DELTA_SO_AVAIL_01' service_order_no, 1 service_type,
             '陪玩目标：测试关卡，设备 PC，预计 2 小时完成' summary_text UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_02', 'TEST_DELTA_SO_AVAIL_02', 2,
             '护航目标：测试段位，设备 PC，今晚 20:00 后' UNION ALL
      SELECT 'TEST_ORDER_CM_AVAIL_03', 'TEST_DELTA_SO_AVAIL_03', 3,
             '趣味单目标：测试玩法，设备 PC，仅用于本地验证' UNION ALL
      SELECT 'TEST_ORDER_CM_CLAIMED_01', 'TEST_DELTA_SO_CLAIMED_01', 1,
             '陪玩目标：已接挂牌展示测试，设备 PC'
    ) scenario
    JOIN trade_order trade ON trade.no = scenario.trade_no AND trade.deleted = 0
    JOIN trade_order_item item ON item.order_id = trade.id AND item.deleted = 0
    JOIN delta_product_service_config config
      ON config.tenant_id = @platform_tenant_id
      AND config.sku_id = item.sku_id
      AND config.service_type = scenario.service_type
      AND config.enabled = 1 AND config.deleted = 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_service_order service_order
      WHERE service_order.tenant_id = @platform_tenant_id
        AND service_order.service_order_no = scenario.service_order_no
        AND service_order.deleted = 0
    )
      AND NOT EXISTS (
        SELECT 1 FROM delta_service_order service_order
        WHERE service_order.trade_order_item_id = item.id AND service_order.deleted = 0
      );

    INSERT INTO delta_order_market_listing
      (listing_no, service_order_id, service_order_no, source_tenant_id,
       service_type, service_amount, requirement_summary, listing_status,
       publish_time, expire_time, claimed_club_id, claimed_club_tenant_id, claim_time,
       publisher_id, withdraw_reason, remark, active_flag, version,
       creator, create_time, updater, update_time, deleted)
    SELECT scenario.listing_no, service_order.id, service_order.service_order_no,
           service_order.tenant_id, service_order.service_type, service_order.service_amount,
           scenario.summary_text, scenario.listing_status,
           DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY),
           CASE WHEN scenario.listing_status = 1 THEN @club_id ELSE NULL END,
           CASE WHEN scenario.listing_status = 1 THEN @club_tenant_id ELSE NULL END,
           CASE WHEN scenario.listing_status = 1 THEN DATE_SUB(NOW(), INTERVAL 30 MINUTE) ELSE NULL END,
           @platform_admin_id, '', 'TEST_DELTA_ 可安全回滚',
           CASE WHEN scenario.listing_status = 0 THEN 1 ELSE 0 END,
           CASE WHEN scenario.listing_status = 0 THEN 0 ELSE 1 END,
           @test_marker, NOW(), @test_marker, NOW(), 0
    FROM (
      SELECT 'TEST_MARKET_REVIEW_AVAILABLE_01' listing_no,
             'TEST_DELTA_SO_AVAIL_01' service_order_no, 0 listing_status,
             '陪玩目标：测试关卡，设备 PC，预计 2 小时完成' summary_text UNION ALL
      SELECT 'TEST_MARKET_REVIEW_AVAILABLE_02', 'TEST_DELTA_SO_AVAIL_02', 0,
             '护航目标：测试段位，设备 PC，今晚 20:00 后' UNION ALL
      SELECT 'TEST_MARKET_REVIEW_AVAILABLE_03', 'TEST_DELTA_SO_AVAIL_03', 0,
             '趣味单目标：测试玩法，设备 PC，仅用于本地验证' UNION ALL
      SELECT 'TEST_MARKET_REVIEW_CLAIMED_01', 'TEST_DELTA_SO_CLAIMED_01', 1,
             '陪玩目标：已接挂牌展示测试，设备 PC'
    ) scenario
    JOIN delta_service_order service_order
      ON service_order.tenant_id = @platform_tenant_id
      AND service_order.service_order_no = scenario.service_order_no
      AND service_order.deleted = 0
    WHERE NOT EXISTS (
      SELECT 1 FROM delta_order_market_listing listing
      WHERE listing.listing_no = scenario.listing_no AND listing.deleted = 0
    )
      AND NOT EXISTS (
        SELECT 1 FROM delta_order_market_listing listing
        WHERE listing.service_order_id = service_order.id
          AND listing.active_flag = 1 AND listing.deleted = 0
      );

    -- 直接 SQL 只生成页面所需市场日志，不伪造 Outbox/通知已发送。
    INSERT INTO delta_order_market_log
      (listing_id, service_order_id, operation_type, operator_type, operator_id,
       club_id, club_tenant_id, before_status, after_status,
       success, failure_reason, remark,
       creator, create_time, updater, update_time, deleted)
    SELECT listing.id, listing.service_order_id, 'PUBLISH', 'PLATFORM', @platform_admin_id,
           NULL, NULL, NULL, NULL, 1, '', 'TEST_DELTA_ 直接 SQL 发布日志',
           @test_marker, listing.publish_time, @test_marker, listing.publish_time, 0
    FROM delta_order_market_listing listing
    WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_'
      AND listing.deleted = 0
      AND NOT EXISTS (
        SELECT 1 FROM delta_order_market_log market_log
        WHERE market_log.listing_id = listing.id
          AND market_log.operation_type = 'PUBLISH'
          AND market_log.creator = @test_marker AND market_log.deleted = 0
      );

    INSERT INTO delta_order_market_log
      (listing_id, service_order_id, operation_type, operator_type, operator_id,
       club_id, club_tenant_id, before_status, after_status,
       success, failure_reason, remark,
       creator, create_time, updater, update_time, deleted)
    SELECT listing.id, listing.service_order_id, 'CLAIM', 'CLUB', NULL,
           listing.claimed_club_id, listing.claimed_club_tenant_id,
           0, 1, 1, '', 'TEST_DELTA_ 直接 SQL 抢单日志',
           @test_marker, listing.claim_time, @test_marker, listing.claim_time, 0
    FROM delta_order_market_listing listing
    WHERE listing.listing_no = 'TEST_MARKET_REVIEW_CLAIMED_01'
      AND listing.listing_status = 1 AND listing.deleted = 0
      AND NOT EXISTS (
        SELECT 1 FROM delta_order_market_log market_log
        WHERE market_log.listing_id = listing.id
          AND market_log.operation_type = 'CLAIM'
          AND market_log.creator = @test_marker AND market_log.deleted = 0
      );

    -- 幂等重跑时不覆盖被人工操作过的数据；状态不再符合预期则整体回滚并提示先执行回滚脚本。
    IF (SELECT COUNT(*) FROM delta_club_application
        WHERE application_no = 'TEST_CLUB_APP_PENDING'
          AND application_status = 0 AND approved_tenant_id IS NULL AND deleted = 0) <> 1
       OR (SELECT COUNT(*) FROM delta_club_application
           WHERE application_no = 'TEST_CLUB_APP_APPROVED'
             AND application_status = 1 AND approved_tenant_id = @club_tenant_id AND deleted = 0) <> 1 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '测试申请已被修改，请先执行回滚 SQL';
    END IF;
    IF (SELECT COUNT(*) FROM delta_club_profile
        WHERE id = @club_id AND tenant_id = @club_tenant_id
          AND business_status = 1 AND application_id = @approved_application_id
          AND deleted = 0) <> 1
       OR (SELECT COUNT(*) FROM delta_club_service_scope
           WHERE tenant_id = @club_tenant_id AND club_profile_id = @club_id
             AND enabled = 1 AND service_type IN (1,2,3) AND deleted = 0) <> 3 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '测试俱乐部或服务范围不完整';
    END IF;
    IF (SELECT COUNT(*) FROM delta_order_market_listing
        WHERE LEFT(listing_no, 19) = 'TEST_MARKET_REVIEW_'
          AND listing_status = 0 AND active_flag = 1
          AND expire_time > NOW() AND deleted = 0) <> 3
       OR (SELECT COUNT(*) FROM delta_order_market_listing
           WHERE listing_no = 'TEST_MARKET_REVIEW_CLAIMED_01'
             AND listing_status = 1 AND active_flag = 0
             AND claimed_club_id = @club_id
             AND claimed_club_tenant_id = @club_tenant_id
             AND claim_time IS NOT NULL AND deleted = 0) <> 1 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '测试挂牌状态已被修改，请先执行回滚 SQL';
    END IF;

    COMMIT;
  END IF;
END$$
CALL proc_test_delta_club_market_review_init()$$
DROP PROCEDURE IF EXISTS proc_test_delta_club_market_review_init$$
DELIMITER ;

SELECT @schema_ready AS schema_ready,
       @init_failed AS init_failed,
       @init_error_message AS init_error_message,
       IF(@confirm_local_dev = 0, '安全锁未开启，未写入',
          IF(@schema_ready = 0, '结构或正式迁移不完整，未写入',
             IF(@init_failed = 1, '执行失败并已回滚', '执行成功'))) AS execution_result;

-- 审核前验证查询。
SELECT application_no, applicant_member_id, club_name, application_status,
       approved_tenant_id, auditor_id, audit_time, version, create_time
FROM delta_club_application
WHERE application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
ORDER BY application_status, id;

SELECT profile.id AS club_id, profile.tenant_id, profile.club_code,
       profile.club_name, profile.owner_member_id, profile.business_status,
       profile.max_concurrent_orders, profile.application_id,
       GROUP_CONCAT(scope.service_type ORDER BY scope.service_type) AS enabled_service_types
FROM delta_club_profile profile
LEFT JOIN delta_club_service_scope scope
  ON scope.club_profile_id = profile.id AND scope.enabled = 1 AND scope.deleted = 0
WHERE profile.club_code = 'TEST_CLUB_MARKET_REVIEW' AND profile.deleted = 0
GROUP BY profile.id, profile.tenant_id, profile.club_code, profile.club_name,
         profile.owner_member_id, profile.business_status,
         profile.max_concurrent_orders, profile.application_id;

SELECT listing.listing_no, listing.listing_status, listing.active_flag,
       listing.service_type, listing.source_tenant_id,
       listing.claimed_club_id, listing.claimed_club_tenant_id,
       listing.publish_time, listing.expire_time, listing.claim_time,
       service_order.service_order_no, service_order.status AS service_order_status,
       service_order.trade_order_no
FROM delta_order_market_listing listing
JOIN delta_service_order service_order ON service_order.id = listing.service_order_id
WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_'
  AND listing.deleted = 0
ORDER BY listing.listing_status, listing.listing_no;

SELECT 'pending_application' item, COUNT(*) row_count
FROM delta_club_application
WHERE application_no = 'TEST_CLUB_APP_PENDING' AND application_status = 0 AND deleted = 0
UNION ALL SELECT 'approved_application', COUNT(*)
FROM delta_club_application
WHERE application_no = 'TEST_CLUB_APP_APPROVED' AND application_status = 1 AND deleted = 0
UNION ALL SELECT 'enabled_club', COUNT(*)
FROM delta_club_profile
WHERE club_code = 'TEST_CLUB_MARKET_REVIEW' AND business_status = 1 AND deleted = 0
UNION ALL SELECT 'enabled_service_scope', COUNT(*)
FROM delta_club_service_scope scope
JOIN delta_club_profile profile ON profile.id = scope.club_profile_id
WHERE profile.club_code = 'TEST_CLUB_MARKET_REVIEW'
  AND scope.enabled = 1 AND scope.deleted = 0 AND profile.deleted = 0
UNION ALL SELECT 'available_listing', COUNT(*)
FROM delta_order_market_listing
WHERE LEFT(listing_no, 19) = 'TEST_MARKET_REVIEW_'
  AND listing_status = 0 AND active_flag = 1 AND expire_time > NOW() AND deleted = 0
UNION ALL SELECT 'claimed_listing', COUNT(*)
FROM delta_order_market_listing
WHERE listing_no = 'TEST_MARKET_REVIEW_CLAIMED_01'
  AND listing_status = 1 AND active_flag = 0 AND deleted = 0
UNION ALL SELECT 'market_log', COUNT(*)
FROM delta_order_market_log market_log
JOIN delta_order_market_listing listing ON listing.id = market_log.listing_id
WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_'
  AND market_log.deleted = 0 AND listing.deleted = 0;

-- 孤儿检查，预期全部为 0。
SELECT 'listing_without_service_order' check_item, COUNT(*) error_count
FROM delta_order_market_listing listing
LEFT JOIN delta_service_order service_order
  ON service_order.id = listing.service_order_id AND service_order.deleted = 0
WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_'
  AND listing.deleted = 0 AND service_order.id IS NULL
UNION ALL
SELECT 'service_order_without_trade_item', COUNT(*)
FROM delta_service_order service_order
LEFT JOIN trade_order_item item
  ON item.id = service_order.trade_order_item_id AND item.deleted = 0
WHERE service_order.service_order_no IN (
  'TEST_DELTA_SO_AVAIL_01','TEST_DELTA_SO_AVAIL_02',
  'TEST_DELTA_SO_AVAIL_03','TEST_DELTA_SO_CLAIMED_01'
) AND service_order.deleted = 0 AND item.id IS NULL
UNION ALL
SELECT 'claimed_listing_without_club', COUNT(*)
FROM delta_order_market_listing listing
LEFT JOIN delta_club_profile profile
  ON profile.id = listing.claimed_club_id AND profile.deleted = 0
WHERE listing.listing_no = 'TEST_MARKET_REVIEW_CLAIMED_01'
  AND listing.deleted = 0 AND profile.id IS NULL;
