package cn.iocoder.yudao.module.delta.dal.mysql.statistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运营统计查询 Mapper（只读聚合，不写业务表）
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaStatisticsQueryMapper {

    // ====== 订单趋势 ======

    /** 按天统计服务订单 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COUNT(1) AS order_count, "
            + "SUM(CASE WHEN status = 80 THEN 1 ELSE 0 END) AS completed_count, "
            + "COALESCE(SUM(service_amount), 0) AS service_amount, "
            + "COALESCE(SUM(CASE WHEN status = 80 THEN service_amount ELSE 0 END), 0) AS completed_amount "
            + "FROM delta_service_order "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectOrderTrendByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 按周统计服务订单 */
    @Select("SELECT DATE_FORMAT(DATE_SUB(create_time, INTERVAL WEEKDAY(create_time) DAY), '%Y-%m-%d') AS date_val, "
            + "COUNT(1) AS order_count, "
            + "SUM(CASE WHEN status = 80 THEN 1 ELSE 0 END) AS completed_count, "
            + "COALESCE(SUM(service_amount), 0) AS service_amount, "
            + "COALESCE(SUM(CASE WHEN status = 80 THEN service_amount ELSE 0 END), 0) AS completed_amount "
            + "FROM delta_service_order "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectOrderTrendByWeek(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 按月统计服务订单 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS date_val, "
            + "COUNT(1) AS order_count, "
            + "SUM(CASE WHEN status = 80 THEN 1 ELSE 0 END) AS completed_count, "
            + "COALESCE(SUM(service_amount), 0) AS service_amount, "
            + "COALESCE(SUM(CASE WHEN status = 80 THEN service_amount ELSE 0 END), 0) AS completed_amount "
            + "FROM delta_service_order "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectOrderTrendByMonth(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ====== 售后趋势 ======

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COUNT(1) AS after_sale_count "
            + "FROM delta_after_sale "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectAfterSaleTrendByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(DATE_SUB(create_time, INTERVAL WEEKDAY(create_time) DAY), '%Y-%m-%d') AS date_val, "
            + "COUNT(1) AS after_sale_count "
            + "FROM delta_after_sale "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectAfterSaleTrendByWeek(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS date_val, "
            + "COUNT(1) AS after_sale_count "
            + "FROM delta_after_sale "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectAfterSaleTrendByMonth(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ====== 退款趋势 ======

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(refund_amount), 0) AS refund_amount "
            + "FROM delta_refund_record "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRefundTrendByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(DATE_SUB(create_time, INTERVAL WEEKDAY(create_time) DAY), '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(refund_amount), 0) AS refund_amount "
            + "FROM delta_refund_record "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRefundTrendByWeek(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS date_val, "
            + "COALESCE(SUM(refund_amount), 0) AS refund_amount "
            + "FROM delta_refund_record "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRefundTrendByMonth(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ====== 追回趋势 ======

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(recovered_amount), 0) AS recovered_amount "
            + "FROM delta_fund_recovery "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRecoveryTrendByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(DATE_SUB(create_time, INTERVAL WEEKDAY(create_time) DAY), '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(recovered_amount), 0) AS recovered_amount "
            + "FROM delta_fund_recovery "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRecoveryTrendByWeek(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS date_val, "
            + "COALESCE(SUM(recovered_amount), 0) AS recovered_amount "
            + "FROM delta_fund_recovery "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectRecoveryTrendByMonth(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ====== 打手排行 ======

    @Select("SELECT so.assigned_worker_id AS worker_id, "
            + "COUNT(1) AS order_count, "
            + "SUM(CASE WHEN so.status = 80 THEN 1 ELSE 0 END) AS completed_count, "
            + "COALESCE(SUM(so.service_amount), 0) AS service_amount "
            + "FROM delta_service_order so "
            + "WHERE so.assigned_worker_id IS NOT NULL "
            + "AND so.create_time >= #{startTime} AND so.create_time < #{endTime} AND so.deleted = 0 "
            + "GROUP BY so.assigned_worker_id "
            + "ORDER BY ${orderByClause} DESC LIMIT #{limit}")
    List<Map<String, Object>> selectWorkerRanking(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
            @Param("orderByClause") String orderByClause, @Param("limit") Integer limit);

    @Select("SELECT ws.worker_id AS worker_id, "
            + "COALESCE(SUM(ws.worker_amount), 0) AS settlement_amount "
            + "FROM delta_worker_settlement ws "
            + "WHERE ws.settlement_status IN (1, 3) "
            + "AND ws.create_time >= #{startTime} AND ws.create_time < #{endTime} AND ws.deleted = 0 "
            + "GROUP BY ws.worker_id ")
    List<Map<String, Object>> selectWorkerSettlementAmount(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ====== 财务趋势 ======

    /** 财务趋势：服务金额+平台佣金 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(service_amount), 0) AS service_amount, "
            + "COALESCE(SUM(platform_fee), 0) AS platform_fee, "
            + "COALESCE(SUM(worker_amount), 0) AS worker_income "
            + "FROM delta_service_order "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectFinanceTrendServiceByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 财务趋势：结算金额 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(worker_amount), 0) AS settlement_amount, "
            + "COALESCE(SUM(CASE WHEN settlement_status = 3 THEN worker_amount ELSE 0 END), 0) AS paid_amount "
            + "FROM delta_worker_settlement "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectFinanceTrendSettlementByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 财务趋势：退款金额 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(refund_amount), 0) AS refund_amount "
            + "FROM delta_refund_record "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectFinanceTrendRefundByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 财务趋势：追回金额 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date_val, "
            + "COALESCE(SUM(recovered_amount), 0) AS recovered_amount "
            + "FROM delta_fund_recovery "
            + "WHERE create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0 "
            + "GROUP BY date_val ORDER BY date_val ASC")
    List<Map<String, Object>> selectFinanceTrendRecoveryByDay(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

}
