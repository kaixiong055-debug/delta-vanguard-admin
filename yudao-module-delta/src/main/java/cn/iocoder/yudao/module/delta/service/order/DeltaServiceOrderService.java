package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.DeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderTimelineRespVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceCreateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressCreateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderAcceptanceRespVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.DeltaOrderReworkRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAcceptanceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderReworkDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 服务履约订单 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaServiceOrderService {

    // ====== Phase 3 已有 ======

    DeltaServiceOrderDO getServiceOrder(Long id);
    DeltaServiceOrderDO getServiceOrderByNo(String serviceOrderNo);
    DeltaServiceOrderDO getServiceOrderByTradeOrderItemId(Long tradeOrderItemId);
    List<DeltaServiceOrderDO> getServiceOrdersByTradeOrderItemIds(Collection<Long> tradeOrderItemIds);
    List<DeltaServiceOrderDO> batchCreateServiceOrders(List<DeltaServiceOrderDO> orders);
    PageResult<DeltaServiceOrderDO> getServiceOrderPage(Long userId, @Valid AppDeltaServiceOrderPageReqVO pageReqVO);
    DeltaServiceOrderDO getServiceOrderForUser(Long id, Long userId);
    PageResult<DeltaServiceOrderDO> getServiceOrderPage(@Valid DeltaServiceOrderPageReqVO pageReqVO);

    // ====== Phase 4 后台服务单操作 ======

    void confirmServiceOrder(Long id, String remark, Long adminUserId);
    void dispatchOrder(Long serviceOrderId, Long workerId, String remark, Long adminUserId);
    void reassignOrder(Long serviceOrderId, Long newWorkerId, String reason, Long adminUserId);
    void returnOrderToPool(Long serviceOrderId, String reason, Long adminUserId);

    // ====== Phase 4 App 打手操作 ======

    void claimOrder(Long loginUserId, Long serviceOrderId);
    PageResult<DeltaServiceOrderDO> getPoolPage(Long workerId, Integer deviceType, Integer serviceType, PageParam pageParam);
    DeltaServiceOrderDO getPoolDetail(Long id, Long workerId);
    PageResult<DeltaServiceOrderDO> getWorkerOrderPage(Long loginUserId, Integer status, PageParam pageParam);
    DeltaServiceOrderDO getWorkerOrderDetail(Long id, Long loginUserId);

    // ====== Phase 5 打手服务执行 ======

    /**
     * 打手开始服务（ACCEPTED_PENDING_START -> IN_PROGRESS）
     */
    void startService(Long loginUserId, Long serviceOrderId);

    /**
     * 打手提交服务进度
     */
    DeltaOrderProgressDO createProgress(Long loginUserId, AppDeltaWorkerOrderProgressCreateReqVO reqVO);

    /**
     * 打手查询服务进度列表
     */
    List<DeltaOrderProgressDO> getWorkerProgressList(Long loginUserId, Long serviceOrderId);

    /**
     * 打手登记服务凭证
     */
    DeltaOrderEvidenceDO createEvidence(Long loginUserId, AppDeltaWorkerOrderEvidenceCreateReqVO reqVO);

    /**
     * 打手删除服务凭证
     */
    void deleteEvidence(Long loginUserId, Long evidenceId);

    /**
     * 打手查询服务凭证列表
     */
    List<DeltaOrderEvidenceDO> getWorkerEvidenceList(Long loginUserId, Long serviceOrderId);

    /**
     * 打手提交服务完成（IN_PROGRESS -> WORKER_SUBMITTED）
     */
    void submitCompletion(Long loginUserId, Long serviceOrderId, String summary);

    // ====== Phase 5 老板查询 ======

    /**
     * 买家查询服务单履约进度
     */
    List<DeltaOrderProgressDO> getBuyerProgressList(Long loginUserId, Long serviceOrderId);

    /**
     * 买家查询服务单凭证
     */
    List<DeltaOrderEvidenceDO> getBuyerEvidenceList(Long loginUserId, Long serviceOrderId);

    /**
     * 买家查询服务单履约时间线
     */
    List<AppDeltaServiceOrderTimelineRespVO> getBuyerTimeline(Long loginUserId, Long serviceOrderId);

    // ====== Phase 5 后台查询 ======

    /**
     * 后台查询服务单进度
     */
    List<DeltaOrderProgressDO> getProgressListByServiceOrderId(Long serviceOrderId);

    /**
     * 后台查询服务单凭证
     */
    List<DeltaOrderEvidenceDO> getEvidenceListByServiceOrderId(Long serviceOrderId);

    /**
     * 后台查看履约时间线
     */
    List<AppDeltaServiceOrderTimelineRespVO> getTimeline(Long serviceOrderId);

    // ====== Phase 6 验收与返工 ======

    /**
     * 老板验收通过（WORKER_SUBMITTED -> COMPLETED）
     */
    void acceptByBuyer(Long loginUserId, Long serviceOrderId, String remark);

    /**
     * 老板要求返工（WORKER_SUBMITTED -> IN_PROGRESS）
     */
    void requestReworkByBuyer(Long loginUserId, Long serviceOrderId, String reason);

    /**
     * 后台验收通过（WORKER_SUBMITTED -> COMPLETED）
     */
    void acceptByAdmin(Long adminUserId, Long serviceOrderId, String remark);

    /**
     * 后台要求返工（WORKER_SUBMITTED -> IN_PROGRESS）
     */
    void requestReworkByAdmin(Long adminUserId, Long serviceOrderId, String reason);

    /**
     * 查询验收记录列表
     */
    List<DeltaOrderAcceptanceRespVO> getAcceptanceList(Long serviceOrderId);

    /**
     * 查询返工记录列表
     */
    List<DeltaOrderReworkRespVO> getReworkList(Long serviceOrderId);

    // ====== Phase 8 取消与售后 ======

    /** 买家提交取消申请 */
    Long applyCancelByBuyer(Long buyerUserId, Long serviceOrderId, String reason, String remark);

    /** 买家查询取消申请分页 */
    PageResult<cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaCancelRespVO>
            getBuyerCancelPage(Long buyerUserId, PageParam pageParam);

    /** 买家查询取消申请详情 */
    cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaCancelRespVO
            getBuyerCancelDetail(Long buyerUserId, Long cancelId);

    /** 买家提交售后申请 */
    Long applyAfterSaleByBuyer(Long buyerUserId, Long serviceOrderId, Integer afterSaleType,
                                Integer reasonType, String reason, String description,
                                Integer requestedRefundAmount, String evidenceUrls);

    /** 买家查询售后案件分页 */
    PageResult<cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaAfterSaleRespVO>
            getBuyerAfterSalePage(Long buyerUserId, Integer status, PageParam pageParam);

    /** 买家查询售后案件详情 */
    cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaAfterSaleRespVO
            getBuyerAfterSaleDetail(Long buyerUserId, Long afterSaleId);

    /** 后台批准取消 */
    void approveCancelByAdmin(Long adminUserId, Long cancelId, Integer refundAmount,
                              Integer responsibilityType, String remark);

    /** 后台驳回取消 */
    void rejectCancelByAdmin(Long adminUserId, Long cancelId, String reason);

    /** 后台受理售后 */
    void acceptAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark);

    /** 后台驳回售后 */
    void rejectAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String reason);

    /** 后台仲裁售后 */
    void arbitrateAfterSaleByAdmin(Long adminUserId, Long afterSaleId, Integer decisionType,
                                    Integer refundAmount, Integer responsibilityType,
                                    Integer workerDeductionAmount, Integer platformBearAmount, String remark);

    /** 后台关闭售后案件 */
    void closeAfterSaleByAdmin(Long adminUserId, Long afterSaleId, String remark);

    /** 后台取消分页 */
    PageResult<cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderCancelDO>
            getCancelPage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaOrderCancelPageReqVO reqVO);

    /** 后台取消详情 */
    cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderCancelDO
            getCancelDetail(Long cancelId);

    /** 后台售后分页 */
    PageResult<cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleDO>
            getAfterSalePage(cn.iocoder.yudao.module.delta.controller.admin.order.vo.DeltaAfterSalePageReqVO reqVO);

    /** 后台售后详情 */
    cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaAfterSaleDO
            getAfterSaleDetail(Long afterSaleId);

    // ====== Phase 9 退款记录查询（买家） ======

    /** 买家查询退款记录分页 */
    PageResult<cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaRefundRespVO>
            getBuyerRefundPage(Long buyerUserId, PageParam pageParam);

    /** 买家查询退款记录详情 */
    cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaRefundRespVO
            getBuyerRefundDetail(Long buyerUserId, Long refundId);

}
