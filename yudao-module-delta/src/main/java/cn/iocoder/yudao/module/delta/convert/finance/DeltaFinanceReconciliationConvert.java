package cn.iocoder.yudao.module.delta.convert.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.DeltaFinanceReconciliationExcelVO;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.DeltaFinanceReconciliationRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.finance.DeltaFinanceReconciliationDO;
import cn.iocoder.yudao.module.delta.enums.finance.DeltaFinanceReconciliationStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务对账 Convert
 */
@Mapper
public interface DeltaFinanceReconciliationConvert {

    DeltaFinanceReconciliationConvert INSTANCE = Mappers.getMapper(DeltaFinanceReconciliationConvert.class);

    DeltaFinanceReconciliationRespVO convert(DeltaFinanceReconciliationDO bean);

    List<DeltaFinanceReconciliationRespVO> convertList(List<DeltaFinanceReconciliationDO> list);

    DeltaFinanceReconciliationExcelVO convertToExcel(DeltaFinanceReconciliationRespVO bean);

    List<DeltaFinanceReconciliationExcelVO> convertToExcelList(List<DeltaFinanceReconciliationRespVO> list);

    default PageResult<DeltaFinanceReconciliationRespVO> convertPage(PageResult<DeltaFinanceReconciliationDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    /** 填充状态名称 */
    default void fillStatusName(DeltaFinanceReconciliationRespVO vo) {
        if (vo == null || vo.getStatus() == null) return;
        Arrays.stream(DeltaFinanceReconciliationStatusEnum.values())
                .filter(e -> e.getStatus().equals(vo.getStatus()))
                .findFirst()
                .ifPresent(e -> vo.setStatusName(e.getName()));
    }

    /** 批量填充状态名称 */
    default void fillStatusName(List<DeltaFinanceReconciliationRespVO> list) {
        if (list == null) return;
        Map<Integer, String> statusMap = Arrays.stream(DeltaFinanceReconciliationStatusEnum.values())
                .collect(Collectors.toMap(DeltaFinanceReconciliationStatusEnum::getStatus,
                        DeltaFinanceReconciliationStatusEnum::getName));
        list.forEach(vo -> {
            if (vo.getStatus() != null) {
                vo.setStatusName(statusMap.getOrDefault(vo.getStatus(), "未知"));
            }
        });
    }

}
