package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderCancelDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 取消申请 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaOrderCancelMapper extends BaseMapperX<DeltaOrderCancelDO> {

    /** 根据服务单ID查询待审核取消申请 */
    default DeltaOrderCancelDO selectPendingByServiceOrderId(Long serviceOrderId) {
        return selectOne(new LambdaQueryWrapperX<DeltaOrderCancelDO>()
                .eq(DeltaOrderCancelDO::getServiceOrderId, serviceOrderId)
                .eq(DeltaOrderCancelDO::getApplyStatus, 0));
    }

    /** 后台分页查询 */
    default PageResult<DeltaOrderCancelDO> selectPage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaOrderCancelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaOrderCancelDO>()
                .eqIfPresent(DeltaOrderCancelDO::getCancelNo, reqVO.getCancelNo())
                .eqIfPresent(DeltaOrderCancelDO::getServiceOrderId, reqVO.getServiceOrderId())
                .eqIfPresent(DeltaOrderCancelDO::getBuyerUserId, reqVO.getBuyerUserId())
                .eqIfPresent(DeltaOrderCancelDO::getApplyStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaOrderCancelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaOrderCancelDO::getId));
    }

    /** 买家分页查询 */
    default PageResult<DeltaOrderCancelDO> selectPageByBuyer(Long buyerUserId, Integer status,
                                                               cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<DeltaOrderCancelDO>()
                .eq(DeltaOrderCancelDO::getBuyerUserId, buyerUserId)
                .eqIfPresent(DeltaOrderCancelDO::getApplyStatus, status)
                .orderByDesc(DeltaOrderCancelDO::getId));
    }

    /** CAS 更新状态 */
    default int updateStatusCas(Long id, Integer newStatus, Integer oldStatus,
                                 java.util.function.Consumer<LambdaUpdateWrapper<DeltaOrderCancelDO>> extra) {
        LambdaUpdateWrapper<DeltaOrderCancelDO> wrapper = new LambdaUpdateWrapper<DeltaOrderCancelDO>()
                .eq(DeltaOrderCancelDO::getId, id)
                .eq(DeltaOrderCancelDO::getApplyStatus, oldStatus)
                .set(DeltaOrderCancelDO::getApplyStatus, newStatus);
        if (extra != null) extra.accept(wrapper);
        return update(null, wrapper);
    }

}
