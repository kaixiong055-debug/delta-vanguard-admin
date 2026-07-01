-- Delta 订单市场本地测试数据回滚
-- 只删除 TEST_* / TEST_DELTA_* 标记的数据；不删除复用的商品、SKU、平台租户或已有管理员。
SET NAMES utf8mb4;
SET @confirm_local_dev := 0;

SELECT DATABASE() AS current_database, @@hostname AS database_server_hostname,
       @confirm_local_dev AS confirm_local_dev,
       '确认本地开发库后手工改为 1' AS safety_notice;

SELECT 'market_log' table_name, COUNT(*) row_count
FROM delta_order_market_log ml JOIN delta_order_market_listing l ON l.id = ml.listing_id
WHERE LEFT(l.listing_no, 12) = 'TEST_MARKET_'
UNION ALL SELECT 'member_notification', COUNT(*)
FROM delta_member_notification n JOIN delta_event_outbox e ON e.id = n.outbox_event_id
WHERE LEFT(e.biz_key, 19) = 'market:TEST_MARKET_'
UNION ALL SELECT 'event_outbox', COUNT(*) FROM delta_event_outbox WHERE LEFT(biz_key, 19) = 'market:TEST_MARKET_'
UNION ALL SELECT 'market_listing', COUNT(*) FROM delta_order_market_listing WHERE LEFT(listing_no, 12) = 'TEST_MARKET_'
UNION ALL SELECT 'service_order', COUNT(*) FROM delta_service_order WHERE LEFT(service_order_no, 14) = 'TEST_DELTA_SO_'
UNION ALL SELECT 'trade_order', COUNT(*) FROM trade_order WHERE LEFT(no, 11) = 'TEST_ORDER_'
UNION ALL SELECT 'club_profile', COUNT(*) FROM delta_club_profile WHERE LEFT(club_code, 10) = 'TEST_CLUB_'
UNION ALL SELECT 'club_application', COUNT(*) FROM delta_club_application WHERE LEFT(application_no, 14) = 'TEST_CLUB_APP_';

DELIMITER $$
DROP PROCEDURE IF EXISTS proc_test_delta_market_rollback$$
CREATE PROCEDURE proc_test_delta_market_rollback()
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    GET DIAGNOSTICS CONDITION 1 @rollback_error_message = MESSAGE_TEXT;
    ROLLBACK;
    SET @rollback_failed := 1;
  END;
  SET @rollback_failed := 0;
  SET @rollback_error_message := NULL;

  IF @confirm_local_dev = 1 THEN
    START TRANSACTION;

DELETE n
FROM delta_member_notification n
JOIN delta_event_outbox e ON e.id = n.outbox_event_id
WHERE @confirm_local_dev = 1 AND LEFT(e.biz_key, 19) = 'market:TEST_MARKET_';

DELETE FROM delta_event_outbox
WHERE @confirm_local_dev = 1 AND LEFT(biz_key, 19) = 'market:TEST_MARKET_';

DELETE ml
FROM delta_order_market_log ml
JOIN delta_order_market_listing l ON l.id = ml.listing_id
WHERE @confirm_local_dev = 1
  AND LEFT(l.listing_no, 12) = 'TEST_MARKET_';

DELETE FROM delta_order_market_listing
WHERE @confirm_local_dev = 1 AND LEFT(listing_no, 12) = 'TEST_MARKET_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM delta_service_order
WHERE @confirm_local_dev = 1 AND LEFT(service_order_no, 14) = 'TEST_DELTA_SO_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE oi
FROM trade_order_item oi
JOIN trade_order o ON o.id = oi.order_id
WHERE @confirm_local_dev = 1 AND LEFT(o.no, 11) = 'TEST_ORDER_'
  AND oi.creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM trade_order
WHERE @confirm_local_dev = 1 AND LEFT(no, 11) = 'TEST_ORDER_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM delta_product_service_config
WHERE @confirm_local_dev = 1 AND creator = 'TEST_DELTA_ORDER_MARKET_INIT'
  AND NOT EXISTS (
    SELECT 1 FROM delta_service_order so
    WHERE so.sku_id = delta_product_service_config.sku_id AND so.deleted = 0
  );

DELETE FROM delta_club_service_scope
WHERE @confirm_local_dev = 1 AND LEFT(remark, 11) = 'TEST_DELTA_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM delta_club_profile
WHERE @confirm_local_dev = 1 AND LEFT(club_code, 10) = 'TEST_CLUB_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM delta_club_application
WHERE @confirm_local_dev = 1 AND LEFT(application_no, 14) = 'TEST_CLUB_APP_'
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM member_user
WHERE @confirm_local_dev = 1
  AND (LEFT(nickname, 16) = 'TEST_CLUB_OWNER_' OR nickname = 'TEST_DELTA_BUYER')
  AND LEFT(mark, 11) = 'TEST_DELTA_' AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

DELETE FROM system_tenant
WHERE @confirm_local_dev = 1 AND name IN ('TEST_CLUB_CAPACITY','TEST_CLUB_DISABLED')
  AND creator = 'TEST_DELTA_ORDER_MARKET_INIT';

    COMMIT;
  END IF;
END$$
CALL proc_test_delta_market_rollback()$$
DROP PROCEDURE IF EXISTS proc_test_delta_market_rollback$$
DELIMITER ;

SELECT @rollback_failed AS rollback_failed,
       @rollback_error_message AS rollback_error_message,
       IF(@confirm_local_dev = 0, '安全锁未开启，未删除',
          IF(@rollback_failed = 1, '回滚脚本执行失败，删除事务已回滚', '测试数据回滚完成')) AS rollback_result;

SELECT 'remaining_test_listings' check_item, COUNT(*) row_count
FROM delta_order_market_listing WHERE LEFT(listing_no, 12) = 'TEST_MARKET_'
UNION ALL SELECT 'remaining_test_service_orders', COUNT(*) FROM delta_service_order WHERE LEFT(service_order_no, 14) = 'TEST_DELTA_SO_'
UNION ALL SELECT 'remaining_test_trade_orders', COUNT(*) FROM trade_order WHERE LEFT(no, 11) = 'TEST_ORDER_'
UNION ALL SELECT 'remaining_test_clubs', COUNT(*) FROM delta_club_profile WHERE LEFT(club_code, 10) = 'TEST_CLUB_';
