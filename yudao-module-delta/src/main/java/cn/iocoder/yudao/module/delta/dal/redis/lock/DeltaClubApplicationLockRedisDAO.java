package cn.iocoder.yudao.module.delta.dal.redis.lock;

import cn.iocoder.yudao.module.delta.dal.redis.RedisKeyConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.CLUB_APPLICATION_SUBMIT_BUSY;

/**
 * Delta 俱乐部入驻申请分布式锁 Redis DAO
 *
 * @author Delta-Vanguard
 */
@Repository
public class DeltaClubApplicationLockRedisDAO {

    private static final long WAIT_TIME_MILLIS = 3000L;
    private static final long LEASE_TIME_MILLIS = 10000L;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取俱乐部入驻申请提交锁并执行操作
     *
     * @param memberId 会员ID
     * @param supplier 需要加锁执行的业务逻辑
     * @param <T>      返回值类型
     * @return supplier 的返回值
     */
    public <T> T lockAndRun(Long memberId, Supplier<T> supplier) {
        String lockKey = String.format(RedisKeyConstants.CLUB_APPLICATION_SUBMIT_LOCK, memberId);
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_MILLIS, LEASE_TIME_MILLIS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw exception(CLUB_APPLICATION_SUBMIT_BUSY);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(CLUB_APPLICATION_SUBMIT_BUSY);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
