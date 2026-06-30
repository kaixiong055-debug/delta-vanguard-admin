package cn.iocoder.yudao.module.delta.dal.mysql.worker;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerApplicationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打手申请 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaWorkerApplicationMapper extends BaseMapperX<DeltaWorkerApplicationDO> {

    /**
     * 查询用户最近一次申请
     */
    default DeltaWorkerApplicationDO selectLatestByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<DeltaWorkerApplicationDO>()
                .eq(DeltaWorkerApplicationDO::getUserId, userId)
                .orderByDesc(DeltaWorkerApplicationDO::getId)
                .last("LIMIT 1"));
    }

    default java.util.List<DeltaWorkerApplicationDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<DeltaWorkerApplicationDO>()
                .eq(DeltaWorkerApplicationDO::getUserId, userId)
                .orderByDesc(DeltaWorkerApplicationDO::getId));
    }

    default cn.iocoder.yudao.framework.common.pojo.PageResult<DeltaWorkerApplicationDO> selectPage(
            cn.iocoder.yudao.module.delta.controller.admin.workerapplication.vo.DeltaWorkerApplicationPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<DeltaWorkerApplicationDO>()
                .eqIfPresent(DeltaWorkerApplicationDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(DeltaWorkerApplicationDO::getRealName, pageReqVO.getRealName())
                .eqIfPresent(DeltaWorkerApplicationDO::getPhone, pageReqVO.getPhone())
                .eqIfPresent(DeltaWorkerApplicationDO::getDeviceType, pageReqVO.getDeviceType())
                .eqIfPresent(DeltaWorkerApplicationDO::getApplicationStatus, pageReqVO.getApplicationStatus())
                .betweenIfPresent(DeltaWorkerApplicationDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(DeltaWorkerApplicationDO::getId));
    }

}
