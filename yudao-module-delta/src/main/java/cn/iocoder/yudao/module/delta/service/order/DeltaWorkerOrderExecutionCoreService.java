package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceCreateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressCreateReqVO;
import cn.iocoder.yudao.module.delta.enums.order.EvidenceTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ProgressTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 打手服务执行核心事务 Service（独立 Bean 确保 @Transactional 生效）
 * <p>
 * 不能放在 DeltaServiceOrderServiceImpl 内部由 this 调用，
 * 否则 Spring AOP 代理无法拦截，事务不生效。
 *
 * @author Delta-Vanguard
 */
@Service
@Slf4j
public class DeltaWorkerOrderExecutionCoreService {

    /**
     * 单个订单凭证数量上限
     */
    public static final int MAX_EVIDENCE_PER_ORDER = 20;

    @Resource
    private DeltaServiceOrderMapper deltaServiceOrderMapper;
    @Resource
    private DeltaOrderLogService deltaOrderLogService;
    @Resource
    private DeltaOrderProgressService deltaOrderProgressService;
    @Resource
    private DeltaOrderEvidenceService deltaOrderEvidenceService;
    @Resource
    private cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher deltaEventPublisher;
    @Resource
    private DeltaWorkerOrderAccessService deltaWorkerOrderAccessService;

    // ========== 开始服务 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doStartService(DeltaWorkerOrderAccessContext accessContext) {
        DeltaWorkerOrderAccessContext context = deltaWorkerOrderAccessService.revalidate(accessContext);
        DeltaServiceOrderDO order = context.getOrder();
        DeltaWorkerDO worker = context.getWorker();
        Long serviceOrderId = order.getId();
        // 3. 校验状态
        Integer oldStatus = order.getStatus();
        if (!ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus().equals(oldStatus)) {
            if (ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(oldStatus)) {
                throw exception(SERVICE_ORDER_ALREADY_STARTED);
            }
            throw exception(SERVICE_ORDER_STATUS_CANNOT_START);
        }

        // 4. CAS 更新：ACCEPTED_PENDING_START -> IN_PROGRESS，写 startedAt
        LocalDateTime now = LocalDateTime.now();
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                oldStatus,
                wrapper -> wrapper.set(DeltaServiceOrderDO::getStartedAt, now)
        );
        if (rows != 1) {
            throw exception(SERVICE_ORDER_STATUS_CHANGED);
        }

        // 5. 创建系统开始服务进度
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(worker.getId())
                .progressType(ProgressTypeEnum.START_SERVICE.getType())
                .progressPercent(0)
                .content("打手开始服务")
                .build();
        deltaOrderProgressService.createProgress(progress);

        // 6. 写订单日志
        DeltaOrderLogDO logEntry = DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum.WORKER.getType())
                .operatorId(worker.getId())
                .operation("开始服务")
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.IN_PROGRESS.getStatus())
                .content("打手ID=" + worker.getId() + "(" + worker.getDisplayName() + ") 开始服务")
                .build();
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("开始服务成功 serviceOrderId={}, workerId={}", serviceOrderId, worker.getId());

        // Phase 10: 买家 Outbox 与订单一起写在来源租户
        deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_STARTED.getType())
                .tenantId(context.getSourceTenantId())
                .aggregateType("SERVICE_ORDER")
                .aggregateId(serviceOrderId)
                .bizKey("SERVICE_STARTED:" + serviceOrderId + ":" + worker.getId())
                .recipientId(order.getBuyerUserId())
                .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SERVICE_STARTED.getCode())
                .templateParams(Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                .build());
    }

    // ========== 进度与凭证 ==========

    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderProgressDO doCreateProgress(DeltaWorkerOrderAccessContext accessContext,
                                                  AppDeltaWorkerOrderProgressCreateReqVO reqVO) {
        DeltaWorkerOrderAccessContext context = deltaWorkerOrderAccessService.revalidate(accessContext);
        DeltaServiceOrderDO order = context.getOrder();
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_PROGRESS);
        }
        if (!ProgressTypeEnum.PROGRESS_UPDATE.getType().equals(reqVO.getProgressType())
                && !ProgressTypeEnum.EXCEPTION_REPORT.getType().equals(reqVO.getProgressType())) {
            throw exception(PROGRESS_TYPE_FORBIDDEN);
        }
        if (reqVO.getContent() == null || reqVO.getContent().trim().isEmpty()) {
            throw exception(PROGRESS_CONTENT_EMPTY);
        }
        Integer percent = reqVO.getProgressPercent();
        if (percent != null && (percent < 0 || percent > 100)) {
            throw exception(PROGRESS_PERCENT_INVALID);
        }
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(order.getId()).workerId(context.getWorker().getId())
                .progressType(reqVO.getProgressType()).progressPercent(percent)
                .content(reqVO.getContent()).build();
        deltaOrderProgressService.createProgress(progress);
        return progress;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeltaOrderEvidenceDO doCreateEvidence(DeltaWorkerOrderAccessContext accessContext,
                                                  AppDeltaWorkerOrderEvidenceCreateReqVO reqVO) {
        DeltaWorkerOrderAccessContext context = deltaWorkerOrderAccessService.revalidate(accessContext);
        DeltaServiceOrderDO order = context.getOrder();
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_EVIDENCE);
        }
        boolean validType = false;
        for (EvidenceTypeEnum type : EvidenceTypeEnum.values()) {
            if (type.getType().equals(reqVO.getEvidenceType())) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw exception(SERVICE_ORDER_STATUS_CANNOT_EVIDENCE);
        }
        if (reqVO.getFileUrl() == null || reqVO.getFileUrl().trim().isEmpty()) {
            throw exception(EVIDENCE_URL_EMPTY);
        }
        if (deltaOrderEvidenceService.countEvidenceByServiceOrderId(order.getId())
                >= MAX_EVIDENCE_PER_ORDER) {
            throw exception(EVIDENCE_COUNT_EXCEED);
        }
        DeltaOrderEvidenceDO evidence = DeltaOrderEvidenceDO.builder()
                .serviceOrderId(order.getId()).workerId(context.getWorker().getId())
                .evidenceType(reqVO.getEvidenceType()).content(reqVO.getDescription())
                .imageUrls(Collections.singletonList(reqVO.getFileUrl())).build();
        deltaOrderEvidenceService.createEvidence(evidence);
        return evidence;
    }

    @Transactional(rollbackFor = Exception.class)
    public void doDeleteEvidence(DeltaWorkerOrderAccessContext accessContext, Long evidenceId) {
        DeltaWorkerOrderAccessContext context = deltaWorkerOrderAccessService.revalidate(accessContext);
        DeltaServiceOrderDO order = context.getOrder();
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(order.getStatus())) {
            throw exception(EVIDENCE_CANNOT_OPERATE_AFTER_COMPLETE);
        }
        DeltaOrderEvidenceDO evidence = deltaOrderEvidenceService.getEvidence(evidenceId);
        if (evidence == null) {
            throw exception(EVIDENCE_NOT_EXISTS_P5);
        }
        if (!Objects.equals(evidence.getTenantId(), context.getSourceTenantId())
                || !Objects.equals(evidence.getServiceOrderId(), order.getId())) {
            throw exception(WORKER_ORDER_EVIDENCE_TENANT_MISMATCH);
        }
        if (!Objects.equals(evidence.getWorkerId(), context.getWorker().getId())) {
            throw exception(EVIDENCE_NOT_BELONG_TO_WORKER);
        }
        deltaOrderEvidenceService.deleteEvidence(evidenceId);
        deltaOrderLogService.createOrderLog(DeltaOrderLogDO.builder()
                .serviceOrderId(order.getId()).operatorType(OperatorTypeEnum.WORKER.getType())
                .operatorId(context.getWorker().getId()).operation("删除凭证")
                .beforeStatus(order.getStatus()).afterStatus(order.getStatus())
                .content("打手ID=" + context.getWorker().getId() + " 删除凭证ID=" + evidenceId)
                .build());
    }

    // ========== 提交完成 ==========

    @Transactional(rollbackFor = Exception.class)
    public void doSubmitCompletion(DeltaWorkerOrderAccessContext accessContext, String summary) {
        DeltaWorkerOrderAccessContext context = deltaWorkerOrderAccessService.revalidate(accessContext);
        DeltaServiceOrderDO order = context.getOrder();
        DeltaWorkerDO worker = context.getWorker();
        Long serviceOrderId = order.getId();
        // 3. 校验状态
        Integer oldStatus = order.getStatus();
        if (!ServiceOrderStatusEnum.IN_PROGRESS.getStatus().equals(oldStatus)) {
            if (ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus().equals(oldStatus)) {
                throw exception(SERVICE_ORDER_ALREADY_COMPLETED);
            }
            throw exception(SERVICE_ORDER_STATUS_CANNOT_COMPLETE);
        }

        // 4. 校验至少有一条有效凭证
        List<DeltaOrderEvidenceDO> evidences = deltaOrderEvidenceService
                .getEvidenceListByServiceOrderId(serviceOrderId);
        long validEvidenceCount = evidences.stream().count();
        if (validEvidenceCount == 0) {
            throw exception(EVIDENCE_NO_COMPLETION_EVIDENCE);
        }

        // 5. CAS 更新：IN_PROGRESS -> WORKER_SUBMITTED，写 submittedAt
        LocalDateTime now = LocalDateTime.now();
        int rows = deltaServiceOrderMapper.updateStatusCas(
                serviceOrderId,
                ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                oldStatus,
                wrapper -> wrapper.set(DeltaServiceOrderDO::getSubmittedAt, now)
        );
        if (rows != 1) {
            throw exception(SERVICE_ORDER_STATUS_CHANGED);
        }

        // 6. 创建系统提交完成进度
        DeltaOrderProgressDO progress = DeltaOrderProgressDO.builder()
                .serviceOrderId(serviceOrderId)
                .workerId(worker.getId())
                .progressType(ProgressTypeEnum.SUBMIT_COMPLETION.getType())
                .progressPercent(100)
                .content(summary)
                .build();
        deltaOrderProgressService.createProgress(progress);

        // 7. 写订单日志
        DeltaOrderLogDO logEntry = DeltaOrderLogDO.builder()
                .serviceOrderId(serviceOrderId)
                .operatorType(cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum.WORKER.getType())
                .operatorId(worker.getId())
                .operation("提交完成")
                .beforeStatus(oldStatus)
                .afterStatus(ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus())
                .content("打手ID=" + worker.getId() + "(" + worker.getDisplayName() + ") 提交完成, 总结: " + summary)
                .build();
        deltaOrderLogService.createOrderLog(logEntry);

        log.info("提交完成成功 serviceOrderId={}, workerId={}", serviceOrderId, worker.getId());

        // Phase 10: 买家 Outbox 与订单一起写在来源租户
        deltaEventPublisher.publishToBuyer(cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq.builder()
                .eventType(cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum.SERVICE_COMPLETION_SUBMITTED.getType())
                .tenantId(context.getSourceTenantId())
                .aggregateType("SERVICE_ORDER")
                .aggregateId(serviceOrderId)
                .bizKey("SERVICE_COMPLETION_SUBMITTED:" + serviceOrderId + ":" + worker.getId())
                .recipientId(order.getBuyerUserId())
                .templateCode(cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum.SERVICE_SUBMITTED.getCode())
                .templateParams(Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                .build());
    }

}
