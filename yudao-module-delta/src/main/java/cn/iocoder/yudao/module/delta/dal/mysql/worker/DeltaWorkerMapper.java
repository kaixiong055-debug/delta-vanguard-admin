package cn.iocoder.yudao.module.delta.dal.mysql.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 打手资料 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerMapper extends BaseMapperX<DeltaWorkerDO> {

    default DeltaWorkerDO selectByUserId(Long userId) {
        return selectOne(DeltaWorkerDO::getUserId, userId);
    }

    default PageResult<DeltaWorkerDO> selectPage(DeltaWorkerPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<DeltaWorkerDO>()
                .likeIfPresent(DeltaWorkerDO::getWorkerNo, pageReqVO.getWorkerNo())
                .likeIfPresent(DeltaWorkerDO::getDisplayName, pageReqVO.getDisplayName())
                .likeIfPresent(DeltaWorkerDO::getRealName, pageReqVO.getRealName())
                .eqIfPresent(DeltaWorkerDO::getPhone, pageReqVO.getPhone())
                .eqIfPresent(DeltaWorkerDO::getAuditStatus, pageReqVO.getAuditStatus())
                .eqIfPresent(DeltaWorkerDO::getWorkStatus, pageReqVO.getWorkStatus())
                .eqIfPresent(DeltaWorkerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(DeltaWorkerDO::getIsRecommend, pageReqVO.getIsRecommend())
                .eqIfPresent(DeltaWorkerDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(DeltaWorkerDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(DeltaWorkerDO::getId));
    }

    /**
     * CAS 更新打手工作状态
     *
     * @param id             打手ID
     * @param newWorkStatus  新工作状态
     * @param oldWorkStatus  旧工作状态（CAS条件）
     * @return 受影响行数
     */
    default int updateWorkStatusCas(Long id, Integer newWorkStatus, Integer oldWorkStatus) {
        return update(null, new LambdaUpdateWrapper<DeltaWorkerDO>()
                .eq(DeltaWorkerDO::getId, id)
                .eq(DeltaWorkerDO::getWorkStatus, oldWorkStatus)
                .set(DeltaWorkerDO::getWorkStatus, newWorkStatus));
    }

    /** 在俱乐部租户内按技能和空闲状态分页查询可分派打手。 */
    default PageResult<DeltaWorkerDO> selectClubAvailablePage(
            PageParam pageParam, Long clubTenantId, Integer serviceType, Integer deviceType,
            Set<Long> busyWorkerIds) {
        LambdaQueryWrapperX<DeltaWorkerDO> wrapper = new LambdaQueryWrapperX<DeltaWorkerDO>()
                .eq(DeltaWorkerDO::getTenantId, clubTenantId)
                .eq(DeltaWorkerDO::getAuditStatus, WorkerAuditStatusEnum.APPROVED.getStatus())
                .eq(DeltaWorkerDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .eq(DeltaWorkerDO::getWorkStatus, WorkerWorkStatusEnum.ONLINE.getStatus())
                .orderByDesc(DeltaWorkerDO::getScore);
        wrapper.apply("EXISTS (SELECT 1 FROM delta_worker_skill skill "
                        + "WHERE skill.worker_id = delta_worker.id "
                        + "AND skill.tenant_id = {0} AND skill.service_type = {1} "
                        + "AND skill.device_type = {2} AND skill.status = {3} "
                        + "AND skill.deleted = 0)",
                clubTenantId, serviceType, deviceType, CommonStatusEnum.ENABLE.getStatus());
        wrapper.orderByAsc(DeltaWorkerDO::getId);
        if (busyWorkerIds != null && !busyWorkerIds.isEmpty()) {
            wrapper.notIn(DeltaWorkerDO::getId, busyWorkerIds);
        }
        return selectPage(pageParam, wrapper);
    }

}
