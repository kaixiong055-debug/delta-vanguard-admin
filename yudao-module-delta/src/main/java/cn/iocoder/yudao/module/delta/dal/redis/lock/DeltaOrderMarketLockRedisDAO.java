package cn.iocoder.yudao.module.delta.dal.redis.lock;

import cn.iocoder.yudao.module.delta.dal.redis.RedisKeyConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CLAIM_BUSY;

/**
 * Delta 订单市场抢单分布式锁 Redis DAO
 *
 * @author Delta-Vanguard
 */
@Repository
public class DeltaOrderMarketLockRedisDAO {

    private static final long WAIT_TIME_MILLIS = 3000L;
    private static final long LEASE_TIME_MILLIS = 10000L;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取挂牌抢单/指定锁并执行操作
     *
     * @param listingId 挂牌ID
     * @param supplier  需要加锁执行的业务逻辑
     * @param <T>       返回值类型
     * @return supplier 的返回值
     */
    public <T> T lockAndRun(Long listingId, Supplier<T> supplier) {
        String lockKey = String.format(RedisKeyConstants.ORDER_MARKET_CLAIM_LOCK, listingId);
        return executeWithLock(lockKey, supplier);
    }

    /**
     * 获取发布锁并执行操作（防止同一服务订单并发创建多个有效挂牌）
     *
     * @param serviceOrderId 服务订单ID
     * @param supplier       需要加锁执行的业务逻辑
     * @param <T>            返回值类型
     * @return supplier 的返回值
     */
    public <T> T lockAndRunOnPublish(Long serviceOrderId, Supplier<T> supplier) {
        String lockKey = String.format(RedisKeyConstants.ORDER_MARKET_PUBLISH_LOCK, serviceOrderId);
        return executeWithLock(lockKey, supplier);
    }

    private <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_MILLIS, LEASE_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw exception(ORDER_MARKET_CLAIM_BUSY);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(ORDER_MARKET_CLAIM_BUSY);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
