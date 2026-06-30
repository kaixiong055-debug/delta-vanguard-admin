package cn.iocoder.yudao.module.delta.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.DeltaFinanceReconciliationPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.finance.DeltaFinanceReconciliationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * 财务对账 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaFinanceReconciliationMapper extends BaseMapperX<DeltaFinanceReconciliationDO> {

    /** 按对账日期查询 */
    default DeltaFinanceReconciliationDO selectByReconciliationDate(LocalDate reconciliationDate) {
        return selectOne(new LambdaQueryWrapperX<DeltaFinanceReconciliationDO>()
                .eq(DeltaFinanceReconciliationDO::getReconciliationDate, reconciliationDate));
    }

    /** 分页查询 */
    default PageResult<DeltaFinanceReconciliationDO> selectPage(DeltaFinanceReconciliationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeltaFinanceReconciliationDO>()
                .eqIfPresent(DeltaFinanceReconciliationDO::getReconciliationNo, reqVO.getReconciliationNo())
                .eqIfPresent(DeltaFinanceReconciliationDO::getReconciliationDate, reqVO.getReconciliationDate())
                .eqIfPresent(DeltaFinanceReconciliationDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaFinanceReconciliationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaFinanceReconciliationDO::getId));
    }

    /** CAS 更新状态 + 版本号乐观锁 */
    default int updateStatusCas(Long id, Integer newStatus, Integer expectedStatus, Integer expectedVersion,
                                 Consumer<LambdaUpdateWrapper<DeltaFinanceReconciliationDO>> extra) {
        LambdaUpdateWrapper<DeltaFinanceReconciliationDO> wrapper = new LambdaUpdateWrapper<DeltaFinanceReconciliationDO>()
                .eq(DeltaFinanceReconciliationDO::getId, id)
                .eq(DeltaFinanceReconciliationDO::getStatus, expectedStatus)
                .eq(DeltaFinanceReconciliationDO::getVersion, expectedVersion)
                .set(DeltaFinanceReconciliationDO::getStatus, newStatus)
                .set(DeltaFinanceReconciliationDO::getVersion, expectedVersion + 1);
        if (extra != null) {
            extra.accept(wrapper);
        }
        return update(null, wrapper);
    }

    /** 导出查询（带过滤条件，限定最大条数） */
    default List<DeltaFinanceReconciliationDO> selectListForExport(
            String reconciliationNo, LocalDate reconciliationDate, Integer status,
            LocalDateTime[] createTime, int maxCount) {
        return selectList(new LambdaQueryWrapperX<DeltaFinanceReconciliationDO>()
                .eqIfPresent(DeltaFinanceReconciliationDO::getReconciliationNo, reconciliationNo)
                .eqIfPresent(DeltaFinanceReconciliationDO::getReconciliationDate, reconciliationDate)
                .eqIfPresent(DeltaFinanceReconciliationDO::getStatus, status)
                .betweenIfPresent(DeltaFinanceReconciliationDO::getCreateTime, createTime)
                .orderByDesc(DeltaFinanceReconciliationDO::getId)
                .last("LIMIT " + maxCount));
    }

}
