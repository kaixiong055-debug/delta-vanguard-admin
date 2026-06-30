package cn.iocoder.yudao.module.delta.dal.mysql.event;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaReminderRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 提醒记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaReminderRecordMapper extends BaseMapperX<DeltaReminderRecordDO> {

    /**
     * 按唯一键查询提醒记录（冷却检查）
     */
    default DeltaReminderRecordDO selectForCooldown(String reminderType, String bizType, Long bizId,
                                                      Long recipientId, Long tenantId) {
        return selectOne(new LambdaQueryWrapperX<DeltaReminderRecordDO>()
                .eq(DeltaReminderRecordDO::getReminderType, reminderType)
                .eq(DeltaReminderRecordDO::getBizType, bizType)
                .eq(DeltaReminderRecordDO::getBizId, bizId)
                .eq(DeltaReminderRecordDO::getRecipientId, recipientId)
                .eq(DeltaReminderRecordDO::getTenantId, tenantId));
    }

    /**
     * 更新提醒记录（时间+次数）
     */
    default int updateReminderTime(Long id, LocalDateTime remindTime) {
        DeltaReminderRecordDO record = selectById(id);
        if (record == null) {
            return 0;
        }
        record.setLastRemindTime(remindTime);
        record.setRemindCount(record.getRemindCount() + 1);
        return updateById(record);
    }
}
