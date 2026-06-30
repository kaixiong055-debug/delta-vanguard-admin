package cn.iocoder.yudao.module.delta.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.delta.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Delta 编号生成 Redis DAO
 *
 * @author Delta-Vanguard
 */
@Repository
public class DeltaNoRedisDAO {

    /**
     * 打手编号前缀
     */
    public static final String WORKER_NO_PREFIX = "DW";

    /**
     * 服务订单号前缀
     */
    public static final String SERVICE_ORDER_NO_PREFIX = "DSO";

    /**
     * 结算单号前缀
     */
    public static final String SETTLEMENT_NO_PREFIX = "DSU";
    /**
     * 取消单号前缀
     */
    public static final String CANCEL_NO_PREFIX = "DCN";
    /**
     * 售后单号前缀
     */
    public static final String AFTER_SALE_NO_PREFIX = "DAS";
    /**
     * 退款单号前缀
     */
    public static final String REFUND_NO_PREFIX = "DRF";
    /**
     * 追回单号前缀
     */
    public static final String FUND_RECOVERY_NO_PREFIX = "DFR";
    /**
     * 俱乐部申请编号前缀
     */
    public static final String CLUB_APPLICATION_NO_PREFIX = "DCA";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号
     *
     * @param prefix 前缀（如 DW / DSO / DSU）
     * @return 序号，格式：{prefix} + yyyyMMddHHmmss + 自增序号
     */
    public String generate(String prefix) {
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        String key = RedisKeyConstants.WORKER_NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofMinutes(1L));
        return noPrefix + no;
    }

    /**
     * 生成打手编号
     */
    public String generateWorkerNo() {
        return generate(WORKER_NO_PREFIX);
    }

    /**
     * 生成服务订单号
     */
    public String generateServiceOrderNo() {
        return generate(SERVICE_ORDER_NO_PREFIX);
    }

    /**
     * 生成结算单号
     */
    public String generateSettlementNo() {
        return generate(SETTLEMENT_NO_PREFIX);
    }

    /**
     * 生成取消单号
     */
    public String generateCancelNo() {
        return generate(CANCEL_NO_PREFIX);
    }

    /**
     * 生成售后单号
     */
    public String generateAfterSaleNo() {
        return generate(AFTER_SALE_NO_PREFIX);
    }

    /**
     * 生成退款单号
     */
    public String generateRefundNo() {
        return generate(REFUND_NO_PREFIX);
    }

    /**
     * 生成追回单号
     */
    public String generateFundRecoveryNo() {
        return generate(FUND_RECOVERY_NO_PREFIX);
    }

    /**
     * 生成事件编号
     */
    public String generateEventNo() {
        return generate("DEV");
    }

    /**
     * 生成俱乐部申请编号
     */
    public String generateClubApplicationNo() {
        return generate(CLUB_APPLICATION_NO_PREFIX);
    }

    /**
     * 生成俱乐部编码
     */
    public String generateClubCode() {
        return generate("DC");
    }

    /**
     * 生成市场挂牌编号
     */
    public String generateMarketListingNo() {
        return generate("DML");
    }

}
