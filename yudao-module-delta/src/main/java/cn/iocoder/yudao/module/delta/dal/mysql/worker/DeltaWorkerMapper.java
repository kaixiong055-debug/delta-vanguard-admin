package cn.iocoder.yudao.module.delta.dal.mysql.worker;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.DeltaWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

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

}
