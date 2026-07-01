-- Delta 俱乐部入驻 + 平台订单市场审核数据回滚
-- 默认安全锁关闭；只删除本脚本 TEST_* 数据以及测试期间由真实接口产生的关联日志/事件。

SET NAMES utf8mb4;
SET @confirm_local_dev := 0;
SET @test_marker := 'TEST_DELTA_CLUB_MARKET_REVIEW';

SELECT DATABASE() AS current_database,
       @@hostname AS database_server_hostname,
       @confirm_local_dev AS confirm_local_dev,
       '确认 JDBC 为本地/开发库后，才可手工改为 1' AS safety_notice;

-- 删除前统计。
SELECT 'member_notification' table_name, COUNT(*) row_count
FROM delta_member_notification notification
JOIN delta_event_outbox outbox_event ON outbox_event.id = notification.outbox_event_id
WHERE LEFT(outbox_event.biz_key, 26) = 'market:TEST_MARKET_REVIEW_'
   OR (outbox_event.aggregate_type = 'CLUB_APPLICATION' AND EXISTS (
        SELECT 1 FROM delta_club_application application
        WHERE application.id = outbox_event.aggregate_id
          AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
      ))
   OR (outbox_event.aggregate_type = 'CLUB_PROFILE' AND EXISTS (
        SELECT 1 FROM delta_club_profile profile
        JOIN delta_club_application application ON application.id = profile.application_id
        WHERE profile.id = outbox_event.aggregate_id
          AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
      ))
UNION ALL SELECT 'event_outbox', COUNT(*)
FROM delta_event_outbox outbox_event
WHERE LEFT(outbox_event.biz_key, 26) = 'market:TEST_MARKET_REVIEW_'
   OR (outbox_event.aggregate_type = 'CLUB_APPLICATION' AND EXISTS (
        SELECT 1 FROM delta_club_application application
        WHERE application.id = outbox_event.aggregate_id
          AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
      ))
   OR (outbox_event.aggregate_type = 'CLUB_PROFILE' AND EXISTS (
        SELECT 1 FROM delta_club_profile profile
        JOIN delta_club_application application ON application.id = profile.application_id
        WHERE profile.id = outbox_event.aggregate_id
          AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
      ))
UNION ALL SELECT 'market_log', COUNT(*)
FROM delta_order_market_log market_log
JOIN delta_order_market_listing listing ON listing.id = market_log.listing_id
WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_'
UNION ALL SELECT 'market_listing', COUNT(*)
FROM delta_order_market_listing WHERE LEFT(listing_no, 19) = 'TEST_MARKET_REVIEW_'
UNION ALL SELECT 'service_order', COUNT(*)
FROM delta_service_order
WHERE service_order_no IN (
  'TEST_DELTA_SO_AVAIL_01','TEST_DELTA_SO_AVAIL_02',
  'TEST_DELTA_SO_AVAIL_03','TEST_DELTA_SO_CLAIMED_01'
)
UNION ALL SELECT 'trade_order', COUNT(*)
FROM trade_order WHERE LEFT(no, 14) = 'TEST_ORDER_CM_'
UNION ALL SELECT 'club_service_scope', COUNT(*)
FROM delta_club_service_scope WHERE creator = @test_marker
UNION ALL SELECT 'club_profile', COUNT(*)
FROM delta_club_profile WHERE club_code = 'TEST_CLUB_MARKET_REVIEW'
UNION ALL SELECT 'club_application', COUNT(*)
FROM delta_club_application
WHERE application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED');

DELIMITER $$
DROP PROCEDURE IF EXISTS proc_test_delta_club_market_review_rollback$$
CREATE PROCEDURE proc_test_delta_club_market_review_rollback()
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

    DELETE notification
    FROM delta_member_notification notification
    JOIN delta_event_outbox outbox_event ON outbox_event.id = notification.outbox_event_id
    WHERE LEFT(outbox_event.biz_key, 26) = 'market:TEST_MARKET_REVIEW_'
       OR (outbox_event.aggregate_type = 'CLUB_APPLICATION' AND EXISTS (
            SELECT 1 FROM delta_club_application application
            WHERE application.id = outbox_event.aggregate_id
              AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
          ))
       OR (outbox_event.aggregate_type = 'CLUB_PROFILE' AND EXISTS (
            SELECT 1 FROM delta_club_profile profile
            JOIN delta_club_application application ON application.id = profile.application_id
            WHERE profile.id = outbox_event.aggregate_id
              AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
          ));

    DELETE outbox_event
    FROM delta_event_outbox outbox_event
    WHERE LEFT(outbox_event.biz_key, 26) = 'market:TEST_MARKET_REVIEW_'
       OR (outbox_event.aggregate_type = 'CLUB_APPLICATION' AND EXISTS (
            SELECT 1 FROM delta_club_application application
            WHERE application.id = outbox_event.aggregate_id
              AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
          ))
       OR (outbox_event.aggregate_type = 'CLUB_PROFILE' AND EXISTS (
            SELECT 1 FROM delta_club_profile profile
            JOIN delta_club_application application ON application.id = profile.application_id
            WHERE profile.id = outbox_event.aggregate_id
              AND application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
          ));

    DELETE market_log
    FROM delta_order_market_log market_log
    JOIN delta_order_market_listing listing ON listing.id = market_log.listing_id
    WHERE LEFT(listing.listing_no, 19) = 'TEST_MARKET_REVIEW_';

    DELETE FROM delta_order_market_listing
    WHERE LEFT(listing_no, 19) = 'TEST_MARKET_REVIEW_'
      AND creator = @test_marker;

    DELETE FROM delta_service_order
    WHERE service_order_no IN (
      'TEST_DELTA_SO_AVAIL_01','TEST_DELTA_SO_AVAIL_02',
      'TEST_DELTA_SO_AVAIL_03','TEST_DELTA_SO_CLAIMED_01'
    ) AND creator = @test_marker;

    DELETE item
    FROM trade_order_item item
    JOIN trade_order trade ON trade.id = item.order_id
    WHERE LEFT(trade.no, 14) = 'TEST_ORDER_CM_'
      AND item.creator = @test_marker;

    DELETE FROM trade_order
    WHERE LEFT(no, 14) = 'TEST_ORDER_CM_'
      AND creator = @test_marker;

    DELETE FROM delta_product_service_config
    WHERE creator = @test_marker
      AND sku_id IN (2, 3, 4)
      AND NOT EXISTS (
        SELECT 1 FROM delta_service_order service_order
        WHERE service_order.sku_id = delta_product_service_config.sku_id
          AND service_order.deleted = 0
      );

    DELETE scope
    FROM delta_club_service_scope scope
    JOIN delta_club_profile profile ON profile.id = scope.club_profile_id
    JOIN delta_club_application application ON application.id = profile.application_id
    WHERE application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED');

    DELETE profile
    FROM delta_club_profile profile
    JOIN delta_club_application application ON application.id = profile.application_id
    WHERE application.application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED');

    DELETE FROM delta_club_application
    WHERE application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
      AND creator = @test_marker;

    COMMIT;
  END IF;
END$$
CALL proc_test_delta_club_market_review_rollback()$$
DROP PROCEDURE IF EXISTS proc_test_delta_club_market_review_rollback$$
DELIMITER ;

SELECT @rollback_failed AS rollback_failed,
       @rollback_error_message AS rollback_error_message,
       IF(@confirm_local_dev = 0, '安全锁未开启，未删除',
          IF(@rollback_failed = 1, '回滚失败，删除事务已回滚', '测试数据回滚完成')) AS rollback_result;

SELECT 'remaining_application' check_item, COUNT(*) row_count
FROM delta_club_application
WHERE application_no IN ('TEST_CLUB_APP_PENDING', 'TEST_CLUB_APP_APPROVED')
UNION ALL SELECT 'remaining_club', COUNT(*)
FROM delta_club_profile WHERE club_code = 'TEST_CLUB_MARKET_REVIEW'
UNION ALL SELECT 'remaining_listing', COUNT(*)
FROM delta_order_market_listing WHERE LEFT(listing_no, 19) = 'TEST_MARKET_REVIEW_'
UNION ALL SELECT 'remaining_service_order', COUNT(*)
FROM delta_service_order
WHERE service_order_no IN (
  'TEST_DELTA_SO_AVAIL_01','TEST_DELTA_SO_AVAIL_02',
  'TEST_DELTA_SO_AVAIL_03','TEST_DELTA_SO_CLAIMED_01'
);
