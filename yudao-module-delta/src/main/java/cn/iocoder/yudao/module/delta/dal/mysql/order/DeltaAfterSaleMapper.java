package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 售后案件 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaAfterSaleMapper extends BaseMapperX<DeltaAfterSaleDO> {

    /** 查询未结束的售后案件 */
    default DeltaAfterSaleDO selectActiveByServiceOrderId(Long serviceOrderId) {
        return selectOne(new LambdaQueryWrapperX<DeltaAfterSaleDO>()
                .eq(DeltaAfterSaleDO::getServiceOrderId, serviceOrderId)
                .notIn(DeltaAfterSaleDO::getStatus, 2, 4));  // 排除已驳回和已关闭
    }

    /** 后台分页查询 */
    default PageResult<DeltaAfterSaleDO> selectPage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaAfterSalePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaAfterSaleDO>()
                .eqIfPresent(DeltaAfterSaleDO::getAfterSaleNo, reqVO.getAfterSaleNo())
                .eqIfPresent(DeltaAfterSaleDO::getServiceOrderId, reqVO.getServiceOrderId())
                .eqIfPresent(DeltaAfterSaleDO::getBuyerUserId, reqVO.getBuyerUserId())
                .eqIfPresent(DeltaAfterSaleDO::getWorkerId, reqVO.getWorkerId())
                .eqIfPresent(DeltaAfterSaleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaAfterSaleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaAfterSaleDO::getId));
    }

    /** 买家分页查询 */
    default PageResult<DeltaAfterSaleDO> selectPageByBuyer(Long buyerUserId, Integer status,
                                                             cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<DeltaAfterSaleDO>()
                .eq(DeltaAfterSaleDO::getBuyerUserId, buyerUserId)
                .eqIfPresent(DeltaAfterSaleDO::getStatus, status)
                .orderByDesc(DeltaAfterSaleDO::getId));
    }

    /** CAS 更新状态 */
    default int updateStatusCas(Long id, Integer newStatus, Integer oldStatus,
                                 java.util.function.Consumer<LambdaUpdateWrapper<DeltaAfterSaleDO>> extra) {
        LambdaUpdateWrapper<DeltaAfterSaleDO> wrapper = new LambdaUpdateWrapper<DeltaAfterSaleDO>()
                .eq(DeltaAfterSaleDO::getId, id)
                .eq(DeltaAfterSaleDO::getStatus, oldStatus)
                .set(DeltaAfterSaleDO::getStatus, newStatus);
        if (extra != null) extra.accept(wrapper);
        return update(null, wrapper);
    }

}
