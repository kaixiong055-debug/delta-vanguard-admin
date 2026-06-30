package cn.iocoder.yudao.module.delta.dal.mysql.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaMemberNotificationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 会员站内通知 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaMemberNotificationMapper extends BaseMapperX<DeltaMemberNotificationDO> {

    /**
     * 分页查询用户通知
     */
    default PageResult<DeltaMemberNotificationDO> selectPageByUserId(PageParam pageParam,
                                                                      Long userId, Long tenantId,
                                                                      Boolean readStatus, String notificationType,
                                                                      LocalDateTime[] createTime) {
        LambdaQueryWrapperX<DeltaMemberNotificationDO> wrapper = new LambdaQueryWrapperX<DeltaMemberNotificationDO>()
                .eq(DeltaMemberNotificationDO::getUserId, userId)
                .eq(DeltaMemberNotificationDO::getTenantId, tenantId);
        if (readStatus != null) {
            wrapper.eq(DeltaMemberNotificationDO::getReadStatus, readStatus);
        }
        if (notificationType != null) {
            wrapper.eq(DeltaMemberNotificationDO::getNotificationType, notificationType);
        }
        if (createTime != null && createTime.length == 2 && createTime[0] != null && createTime[1] != null) {
            wrapper.between(DeltaMemberNotificationDO::getCreateTime, createTime[0], createTime[1]);
        }
        wrapper.orderByDesc(DeltaMemberNotificationDO::getId);
        return selectPage(pageParam, wrapper);
    }

    /**
     * 查询未读数量
     */
    default Long selectUnreadCountByUserId(Long userId, Long tenantId) {
        return selectCount(new LambdaQueryWrapperX<DeltaMemberNotificationDO>()
                .eq(DeltaMemberNotificationDO::getUserId, userId)
                .eq(DeltaMemberNotificationDO::getTenantId, tenantId)
                .eq(DeltaMemberNotificationDO::getReadStatus, Boolean.FALSE));
    }

    /**
     * CAS 标记单条已读
     */
    default int updateReadStatus(Long id, Long userId, Long tenantId) {
        LambdaUpdateWrapper<DeltaMemberNotificationDO> wrapper = new LambdaUpdateWrapper<DeltaMemberNotificationDO>()
                .eq(DeltaMemberNotificationDO::getId, id)
                .eq(DeltaMemberNotificationDO::getUserId, userId)
                .eq(DeltaMemberNotificationDO::getTenantId, tenantId)
                .eq(DeltaMemberNotificationDO::getReadStatus, Boolean.FALSE)
                .set(DeltaMemberNotificationDO::getReadStatus, Boolean.TRUE)
                .set(DeltaMemberNotificationDO::getReadTime, LocalDateTime.now());
        return update(null, wrapper);
    }

    /**
     * 标记全部已读（当前用户、当前租户）
     */
    default int updateAllReadStatus(Long userId, Long tenantId) {
        LambdaUpdateWrapper<DeltaMemberNotificationDO> wrapper = new LambdaUpdateWrapper<DeltaMemberNotificationDO>()
                .eq(DeltaMemberNotificationDO::getUserId, userId)
                .eq(DeltaMemberNotificationDO::getTenantId, tenantId)
                .eq(DeltaMemberNotificationDO::getReadStatus, Boolean.FALSE)
                .set(DeltaMemberNotificationDO::getReadStatus, Boolean.TRUE)
                .set(DeltaMemberNotificationDO::getReadTime, LocalDateTime.now());
        return update(null, wrapper);
    }

    /**
     * 按 outboxEventId 查询（幂等检查）
     */
    default DeltaMemberNotificationDO selectByOutboxEventId(Long outboxEventId, Long tenantId) {
        return selectOne(new LambdaQueryWrapperX<DeltaMemberNotificationDO>()
                .eq(DeltaMemberNotificationDO::getOutboxEventId, outboxEventId)
                .eq(DeltaMemberNotificationDO::getTenantId, tenantId));
    }
}
