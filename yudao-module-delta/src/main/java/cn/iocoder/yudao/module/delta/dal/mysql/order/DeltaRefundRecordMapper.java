package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaRefundRecordPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaRefundRecordDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.function.Consumer;

/**
 * 退款记录 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaRefundRecordMapper extends BaseMapperX<DeltaRefundRecordDO> {

    default List<DeltaRefundRecordDO> selectListByAfterSaleId(Long afterSaleId) {
        return selectList(new LambdaQueryWrapperX<DeltaRefundRecordDO>()
                .eq(DeltaRefundRecordDO::getAfterSaleId, afterSaleId)
                .orderByDesc(DeltaRefundRecordDO::getId));
    }

    default DeltaRefundRecordDO selectByAfterSaleId(Long afterSaleId) {
        return selectOne(DeltaRefundRecordDO::getAfterSaleId, afterSaleId);
    }

    default PageResult<DeltaRefundRecordDO> selectPage(DeltaRefundRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaRefundRecordDO>()
                .eqIfPresent(DeltaRefundRecordDO::getRefundNo, reqVO.getRefundNo())
                .eqIfPresent(DeltaRefundRecordDO::getBuyerUserId, reqVO.getBuyerUserId())
                .eqIfPresent(DeltaRefundRecordDO::getRefundStatus, reqVO.getRefundStatus())
                .eqIfPresent(DeltaRefundRecordDO::getRefundMethod, reqVO.getRefundMethod())
                .eqIfPresent(DeltaRefundRecordDO::getHandlerId, reqVO.getHandlerId())
                .betweenIfPresent(DeltaRefundRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaRefundRecordDO::getId));
    }

    /**
     * CAS 更新退款状态
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer expectedStatus,
                                 Consumer<LambdaUpdateWrapper<DeltaRefundRecordDO>> extra) {
        LambdaUpdateWrapper<DeltaRefundRecordDO> wrapper = new LambdaUpdateWrapper<DeltaRefundRecordDO>()
                .eq(DeltaRefundRecordDO::getId, id)
                .eq(DeltaRefundRecordDO::getRefundStatus, expectedStatus)
                .set(DeltaRefundRecordDO::getRefundStatus, newStatus);
        extra.accept(wrapper);
        return update(null, wrapper);
    }

}

