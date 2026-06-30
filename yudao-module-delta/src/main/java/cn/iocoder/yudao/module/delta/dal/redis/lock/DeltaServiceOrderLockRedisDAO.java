package cn.iocoder.yudao.module.delta.dal.redis.lock;

import cn.iocoder.yudao.module.delta.dal.redis.RedisKeyConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ASSIGNMENT_ORDER_BEING_PROCESSED;

/**
 * Delta 服务单分布式锁 Redis DAO
 *
 * @author Delta-Vanguard
 */
@Repository
public class DeltaServiceOrderLockRedisDAO {

    private static final long WAIT_TIME_MILLIS = 3000L;
    private static final long LEASE_TIME_MILLIS = 10000L;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取服务单接单/派单锁并执行操作
     *
     * @param tenantId       租户ID
     * @param serviceOrderId 服务单ID
     * @param supplier       需要加锁执行的业务逻辑
     * @param <T>            返回值类型
     * @return supplier 的返回值
     */
    public <T> T lockAndRun(Long tenantId, Long serviceOrderId, Supplier<T> supplier) {
        String lockKey = formatLockKey(tenantId, serviceOrderId);
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_MILLIS, LEASE_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(ASSIGNMENT_ORDER_BEING_PROCESSED);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String formatLockKey(Long tenantId, Long serviceOrderId) {
        return String.format(RedisKeyConstants.SERVICE_ORDER_CLAIM_LOCK, tenantId, serviceOrderId);
    }

}
