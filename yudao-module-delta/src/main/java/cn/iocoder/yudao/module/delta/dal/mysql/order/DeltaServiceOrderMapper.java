package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.DeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 服务履约订单 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaServiceOrderMapper extends BaseMapperX<DeltaServiceOrderDO> {

    default DeltaServiceOrderDO selectByTradeOrderItemId(Long tradeOrderItemId) {
        return selectOne(DeltaServiceOrderDO::getTradeOrderItemId, tradeOrderItemId);
    }

    default DeltaServiceOrderDO selectByServiceOrderNo(String serviceOrderNo) {
        return selectOne(DeltaServiceOrderDO::getServiceOrderNo, serviceOrderNo);
    }

    default List<DeltaServiceOrderDO> selectListByTradeOrderItemIds(Collection<Long> tradeOrderItemIds) {
        return selectList(DeltaServiceOrderDO::getTradeOrderItemId, tradeOrderItemIds);
    }

    default PageResult<DeltaServiceOrderDO> selectPage(AppDeltaServiceOrderPageReqVO reqVO) {
        return selectPage(reqVO, buildAppQueryWrapper(reqVO));
    }

    default PageResult<DeltaServiceOrderDO> selectPage(DeltaServiceOrderPageReqVO reqVO) {
        return selectPage(reqVO, buildAdminQueryWrapper(reqVO));
    }

    default LambdaQueryWrapper<DeltaServiceOrderDO> buildAppQueryWrapper(AppDeltaServiceOrderPageReqVO reqVO) {
        return new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eqIfPresent(DeltaServiceOrderDO::getBuyerUserId, reqVO.getUserId())
                .eqIfPresent(DeltaServiceOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, reqVO.getServiceType())
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, reqVO.getDeviceType())
                .betweenIfPresent(DeltaServiceOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaServiceOrderDO::getId);
    }

    default LambdaQueryWrapper<DeltaServiceOrderDO> buildAdminQueryWrapper(DeltaServiceOrderPageReqVO reqVO) {
        return new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eqIfPresent(DeltaServiceOrderDO::getServiceOrderNo, reqVO.getServiceOrderNo())
                .eqIfPresent(DeltaServiceOrderDO::getTradeOrderNo, reqVO.getTradeOrderNo())
                .eqIfPresent(DeltaServiceOrderDO::getBuyerUserId, reqVO.getBuyerUserId())
                .eqIfPresent(DeltaServiceOrderDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(DeltaServiceOrderDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, reqVO.getServiceType())
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(DeltaServiceOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaServiceOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaServiceOrderDO::getId);
    }

    /**
     * CAS 条件更新服务单状态
     *
     * @param id       服务单ID
     * @param newStatus 新状态
     * @param oldStatus 旧状态（CAS条件）
     * @param updates  其他需要更新的字段
     * @return 受影响行数
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer oldStatus,
                                 java.util.function.Consumer<LambdaUpdateWrapper<DeltaServiceOrderDO>> updates) {
        LambdaUpdateWrapper<DeltaServiceOrderDO> wrapper = new LambdaUpdateWrapper<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getId, id)
                .eq(DeltaServiceOrderDO::getStatus, oldStatus)
                .set(DeltaServiceOrderDO::getStatus, newStatus);
        if (updates != null) {
            updates.accept(wrapper);
        }
        return update(null, wrapper);
    }

    /**
     * 订单池分页查询（只包含POOL_PENDING状态+未分配打手的订单+技能匹配）
     */
    default PageResult<DeltaServiceOrderDO> selectPoolPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
                                                            Integer poolStatus, Integer deviceType, Integer serviceType) {
        LambdaQueryWrapperX<DeltaServiceOrderDO> wrapper = new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getStatus, poolStatus)
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, deviceType)
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, serviceType)
                .orderByDesc(DeltaServiceOrderDO::getId);
        // isNull 放在最后调用，因为父类 isNull 返回 LambdaQueryWrapper，不支持后续 eqIfPresent 链式调用
        wrapper.isNull(DeltaServiceOrderDO::getAssignedWorkerId);
        return selectPage(pageParam, wrapper);
    }

    /**
     * 打手已分配订单分页查询
     */
    default PageResult<DeltaServiceOrderDO> selectWorkerPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
                                                              Long assignedWorkerId, Integer status) {
        LambdaQueryWrapperX<DeltaServiceOrderDO> wrapper = new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getAssignedWorkerId, assignedWorkerId)
                .eqIfPresent(DeltaServiceOrderDO::getStatus, status)
                .orderByDesc(DeltaServiceOrderDO::getId);
        return selectPage(pageParam, wrapper);
    }

}
