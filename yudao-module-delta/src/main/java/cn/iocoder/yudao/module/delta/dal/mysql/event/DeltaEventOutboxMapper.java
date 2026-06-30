package cn.iocoder.yudao.module.delta.dal.mysql.event;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.event.vo.DeltaEventOutboxPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * 领域事件 Outbox Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaEventOutboxMapper extends BaseMapperX<DeltaEventOutboxDO> {

    /**
     * 按幂等键查询
     */
    default DeltaEventOutboxDO selectByBizKey(String bizKey, Long tenantId) {
        return selectOne(new LambdaQueryWrapperX<DeltaEventOutboxDO>()
                .eq(DeltaEventOutboxDO::getBizKey, bizKey)
                .eq(DeltaEventOutboxDO::getTenantId, tenantId));
    }

    /**
     * 分页查询 Outbox 事件
     */
    default PageResult<DeltaEventOutboxDO> selectPage(DeltaEventOutboxPageReqVO reqVO) {
        LambdaQueryWrapperX<DeltaEventOutboxDO> wrapper = new LambdaQueryWrapperX<DeltaEventOutboxDO>()
                .eqIfPresent(DeltaEventOutboxDO::getEventType, reqVO.getEventType())
                .eqIfPresent(DeltaEventOutboxDO::getEventStatus, reqVO.getEventStatus())
                .eqIfPresent(DeltaEventOutboxDO::getAggregateType, reqVO.getAggregateType())
                .eqIfPresent(DeltaEventOutboxDO::getAggregateId, reqVO.getAggregateId())
                .eqIfPresent(DeltaEventOutboxDO::getRecipientType, reqVO.getRecipientType())
                .orderByDesc(DeltaEventOutboxDO::getId);
        if (reqVO.getCreateTimeStart() != null && reqVO.getCreateTimeEnd() != null) {
            wrapper.between(DeltaEventOutboxDO::getCreateTime, reqVO.getCreateTimeStart(), reqVO.getCreateTimeEnd());
        }
        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询待消费事件（PENDING 或 可重试的 FAILED，且到达重试时间）
     */
    default List<DeltaEventOutboxDO> selectPendingEvents(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<DeltaEventOutboxDO>()
                .and(w -> w.eq(DeltaEventOutboxDO::getEventStatus, 0)  // PENDING
                        .or(w2 -> w2.eq(DeltaEventOutboxDO::getEventStatus, 3)  // FAILED
                                .le(DeltaEventOutboxDO::getNextRetryTime, now)))
                .orderByAsc(DeltaEventOutboxDO::getNextRetryTime)
                .orderByAsc(DeltaEventOutboxDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 查询长时间处于 PROCESSING 状态的事件（用于恢复）
     */
    default List<DeltaEventOutboxDO> selectStuckProcessingEvents(LocalDateTime before, int limit) {
        return selectList(new LambdaQueryWrapperX<DeltaEventOutboxDO>()
                .eq(DeltaEventOutboxDO::getEventStatus, 1)  // PROCESSING
                .le(DeltaEventOutboxDO::getCreateTime, before)
                .last("LIMIT " + limit));
    }

    /**
     * CAS 更新事件状态
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer expectedStatus,
                                 Consumer<LambdaUpdateWrapper<DeltaEventOutboxDO>> extra) {
        LambdaUpdateWrapper<DeltaEventOutboxDO> wrapper = new LambdaUpdateWrapper<DeltaEventOutboxDO>()
                .eq(DeltaEventOutboxDO::getId, id)
                .eq(DeltaEventOutboxDO::getEventStatus, expectedStatus);
        extra.accept(wrapper);
        return update(null, wrapper);
    }

    /**
     * CAS 更新：PENDING/FAILED -> PROCESSING
     */
    default int casStartProcessing(Long id, Integer expectedStatus) {
        return updateStatusCas(id, 1, expectedStatus, wrapper -> {});
    }

    /**
     * CAS 更新：PROCESSING -> SUCCESS
     */
    default int casMarkSuccess(Long id, LocalDateTime processedTime) {
        return updateStatusCas(id, 2, 1, wrapper ->  // SUCCESS, expected PROCESSING
                wrapper.set(DeltaEventOutboxDO::getProcessedTime, processedTime));
    }

    /**
     * CAS 更新：PROCESSING -> FAILED
     */
    default int casMarkFailed(Long id, Integer retryCount, String lastError, LocalDateTime nextRetryTime) {
        return updateStatusCas(id, 3, 1, wrapper -> {  // FAILED, expected PROCESSING
            wrapper.set(DeltaEventOutboxDO::getRetryCount, retryCount);
            wrapper.set(DeltaEventOutboxDO::getLastError, lastError);
            wrapper.set(DeltaEventOutboxDO::getNextRetryTime, nextRetryTime);
        });
    }

    /**
     * CAS 更新：FAILED -> PENDING（人工重试）
     */
    default int casRetry(Long id) {
        return updateStatusCas(id, 0, 3, wrapper -> {});  // PENDING, expected FAILED
    }

    /**
     * CAS 更新：FAILED -> DEAD
     */
    default int casMarkDead(Long id) {
        return updateStatusCas(id, 4, 3, wrapper -> {});  // DEAD, expected FAILED
    }

    /**
     * CAS 更新：PENDING -> DEAD
     */
    default int casMarkDeadFromPending(Long id) {
        return updateStatusCas(id, 4, 0, wrapper -> {});  // DEAD, expected PENDING
    }

    /**
     * 恢复 PROCESSING 超时事件为 FAILED
     */
    default int casRecoverStuckToFailed(Long id, String lastError) {
        return updateStatusCas(id, 3, 1, wrapper ->  // FAILED, expected PROCESSING
                wrapper.set(DeltaEventOutboxDO::getLastError, lastError)
                        .set(DeltaEventOutboxDO::getNextRetryTime, LocalDateTime.now()));
    }
}
