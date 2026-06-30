package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 完成凭证 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderEvidenceMapper extends BaseMapperX<DeltaOrderEvidenceDO> {

    /**
     * 根据服务单ID查询凭证列表（按创建时间正序）
     */
    default List<DeltaOrderEvidenceDO> selectListByServiceOrderId(Long serviceOrderId) {
        return selectList(new LambdaQueryWrapper<DeltaOrderEvidenceDO>()
                .eq(DeltaOrderEvidenceDO::getServiceOrderId, serviceOrderId)
                .orderByAsc(DeltaOrderEvidenceDO::getCreateTime));
    }

    /**
     * 根据服务单ID和打手ID查询凭证列表
     */
    default List<DeltaOrderEvidenceDO> selectListByServiceOrderIdAndWorkerId(Long serviceOrderId, Long workerId) {
        return selectList(new LambdaQueryWrapper<DeltaOrderEvidenceDO>()
                .eq(DeltaOrderEvidenceDO::getServiceOrderId, serviceOrderId)
                .eq(DeltaOrderEvidenceDO::getWorkerId, workerId)
                .orderByAsc(DeltaOrderEvidenceDO::getCreateTime));
    }

    /**
     * 根据服务单ID列表批量查询凭证
     */
    default List<DeltaOrderEvidenceDO> selectListByServiceOrderIds(List<Long> serviceOrderIds) {
        return selectList(new LambdaQueryWrapper<DeltaOrderEvidenceDO>()
                .in(DeltaOrderEvidenceDO::getServiceOrderId, serviceOrderIds)
                .orderByAsc(DeltaOrderEvidenceDO::getCreateTime));
    }

    /**
     * 统计服务单有效凭证数量
     */
    default long selectCountByServiceOrderId(Long serviceOrderId) {
        return selectCount(new LambdaQueryWrapper<DeltaOrderEvidenceDO>()
                .eq(DeltaOrderEvidenceDO::getServiceOrderId, serviceOrderId));
    }

}
