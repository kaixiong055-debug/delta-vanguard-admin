package cn.iocoder.yudao.module.delta.service.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaMemberNotificationDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaMemberNotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Delta 会员站内通知 Service
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaMemberNotificationService {

    @Resource
    private DeltaMemberNotificationMapper deltaMemberNotificationMapper;

    /**
     * 创建站内通知
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(Long userId, String userType, String notificationType,
                                    String title, String content,
                                    String bizType, Long bizId,
                                    Long outboxEventId, Long tenantId) {
        // 幂等检查：同一 outboxEventId 不重复创建
        if (outboxEventId != null) {
            DeltaMemberNotificationDO exist = deltaMemberNotificationMapper.selectByOutboxEventId(outboxEventId, tenantId);
            if (exist != null) {
                log.info("通知已存在，幂等跳过 outboxEventId={}, userId={}", outboxEventId, userId);
                return exist.getId();
            }
        }

        DeltaMemberNotificationDO notification = DeltaMemberNotificationDO.builder()
                .userId(userId)
                .userType(userType)
                .notificationType(notificationType)
                .title(title)
                .content(content)
                .bizType(bizType)
                .bizId(bizId)
                .readStatus(Boolean.FALSE)
                .readTime(null)
                .outboxEventId(outboxEventId)
                .tenantId(tenantId)
                .build();

        deltaMemberNotificationMapper.insert(notification);
        return notification.getId();
    }

    /**
     * 分页查询通知（仅当前用户、当前租户）
     */
    public PageResult<DeltaMemberNotificationDO> getNotificationPage(Long userId, Long tenantId,
                                                                       PageParam pageParam,
                                                                       Boolean readStatus,
                                                                       String notificationType,
                                                                       LocalDateTime[] createTime) {
        return deltaMemberNotificationMapper.selectPageByUserId(
                pageParam, userId, tenantId, readStatus, notificationType, createTime);
    }

    /**
     * 获取通知详情
     */
    public DeltaMemberNotificationDO getNotification(Long id, Long userId, Long tenantId) {
        DeltaMemberNotificationDO notification = deltaMemberNotificationMapper.selectById(id);
        if (notification == null) {
            throw exception(NOTIFICATION_NOT_EXISTS);
        }
        if (!notification.getUserId().equals(userId)) {
            throw exception(NOTIFICATION_NOT_BELONG_TO_USER);
        }
        if (!notification.getTenantId().equals(tenantId)) {
            throw exception(CROSS_TENANT_ACCESS_DENIED);
        }
        return notification;
    }

    /**
     * 标记已读（仅当前用户、当前租户）
     */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id, Long userId, Long tenantId) {
        DeltaMemberNotificationDO notification = deltaMemberNotificationMapper.selectById(id);
        if (notification == null) {
            throw exception(NOTIFICATION_NOT_EXISTS);
        }
        if (!notification.getUserId().equals(userId)) {
            throw exception(NOTIFICATION_NOT_BELONG_TO_USER);
        }
        if (Boolean.TRUE.equals(notification.getReadStatus())) {
            throw exception(NOTIFICATION_ALREADY_READ);
        }
        int rows = deltaMemberNotificationMapper.updateReadStatus(id, userId, tenantId);
        if (rows != 1) {
            throw exception(NOTIFICATION_ALREADY_READ);
        }
    }

    /**
     * 全部标记已读（仅当前用户、当前租户）
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId, Long tenantId) {
        int rows = deltaMemberNotificationMapper.updateAllReadStatus(userId, tenantId);
        log.info("标记已读 userId={}, tenantId={}, rows={}", userId, tenantId, rows);
    }

    /**
     * 查询未读数量
     */
    public Long getUnreadCount(Long userId, Long tenantId) {
        return deltaMemberNotificationMapper.selectUnreadCountByUserId(userId, tenantId);
    }
}
