package cn.iocoder.yudao.module.delta.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.finance.vo.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 财务对账 Service
 */
public interface DeltaFinanceReconciliationService {

    /** 分页查询 */
    PageResult<DeltaFinanceReconciliationRespVO> getReconciliationPage(DeltaFinanceReconciliationPageReqVO reqVO);

    /** 详情 */
    DeltaFinanceReconciliationRespVO getReconciliation(Long id);

    /** 生成对账 */
    Long generateReconciliation(@Valid DeltaFinanceReconciliationGenerateReqVO reqVO);

    /** 确认对账 */
    void confirmReconciliation(@Valid DeltaFinanceReconciliationConfirmReqVO reqVO);

    /** 重试 */
    void retryReconciliation(Long id);

    /** 取消 */
    void cancelReconciliation(@Valid DeltaFinanceReconciliationCancelReqVO reqVO);

    /** 导出查询（带最大条数限制） */
    List<DeltaFinanceReconciliationRespVO> getReconciliationList(
            @Valid DeltaFinanceReconciliationExportReqVO reqVO);

}
