package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderEvidenceCreateReqVO;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressCreateReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.enums.order.ProgressTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaWorkerOrderExecutionCoreServiceTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaWorkerOrderExecutionCoreService service;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaOrderLogService orderLogService;
    @Mock private DeltaOrderProgressService progressService;
    @Mock private DeltaOrderEvidenceService evidenceService;
    @Mock private DeltaEventPublisher eventPublisher;
    @Mock private DeltaWorkerOrderAccessService accessService;

    @BeforeEach
    void setUp() {
        lenient().when(serviceOrderMapper.updateStatusCas(anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(1);
    }

    @Test
    void start_updates40To50AndWritesSourceTenantRecords() throws Exception {
        DeltaWorkerOrderAccessContext context = context(40);
        when(accessService.revalidate(context)).thenReturn(context);
        service.doStartService(context);
        verify(serviceOrderMapper).updateStatusCas(eq(100L), eq(50), eq(40), any());
        verify(progressService).createProgress(argThat(p ->
                ProgressTypeEnum.START_SERVICE.getType().equals(p.getProgressType())
                        && Long.valueOf(5L).equals(p.getWorkerId())));
        verify(orderLogService).createOrderLog(any(DeltaOrderLogDO.class));
        verify(eventPublisher).publishToBuyer(argThat((DeltaEventPublishReq req) ->
                Long.valueOf(40L).equals(req.getTenantId())));
        assertNotNull(DeltaWorkerOrderExecutionCoreService.class
                .getMethod("doStartService", DeltaWorkerOrderAccessContext.class)
                .getAnnotation(Transactional.class));
    }

    @Test
    void start_progressFailurePropagatesAfterCasForTransactionRollback() {
        DeltaWorkerOrderAccessContext context = context(40);
        when(accessService.revalidate(context)).thenReturn(context);
        doThrow(new IllegalStateException("progress failed")).when(progressService)
                .createProgress(any(DeltaOrderProgressDO.class));
        assertThrows(IllegalStateException.class, () -> service.doStartService(context));
        verify(eventPublisher, never()).publishToBuyer(any());
    }

    @Test
    void createProgress_allowsOnlyWorkerProgressTypes() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        AppDeltaWorkerOrderProgressCreateReqVO req = progressReq(ProgressTypeEnum.PROGRESS_UPDATE.getType());
        DeltaOrderProgressDO result = service.doCreateProgress(context, req);
        assertEquals(5L, result.getWorkerId());
        verify(progressService).createProgress(result);

        AppDeltaWorkerOrderProgressCreateReqVO forbidden = progressReq(ProgressTypeEnum.REWORK_REQUEST.getType());
        assertServiceException(() -> service.doCreateProgress(context, forbidden), PROGRESS_TYPE_FORBIDDEN);
    }

    @Test
    void createEvidence_enforcesLimitAndRealWorker() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        when(evidenceService.countEvidenceByServiceOrderId(100L)).thenReturn(0L);
        AppDeltaWorkerOrderEvidenceCreateReqVO req = evidenceReq();
        DeltaOrderEvidenceDO result = service.doCreateEvidence(context, req);
        assertEquals(5L, result.getWorkerId());
        verify(evidenceService).createEvidence(result);

        when(evidenceService.countEvidenceByServiceOrderId(100L)).thenReturn(20L);
        assertServiceException(() -> service.doCreateEvidence(context, req), EVIDENCE_COUNT_EXCEED);
    }

    @Test
    void createEvidence_insertFailurePropagatesWithoutFollowingSideEffects() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        when(evidenceService.countEvidenceByServiceOrderId(100L)).thenReturn(0L);
        doThrow(new IllegalStateException("evidence failed")).when(evidenceService)
                .createEvidence(any(DeltaOrderEvidenceDO.class));
        assertThrows(IllegalStateException.class,
                () -> service.doCreateEvidence(context, evidenceReq()));
        verify(orderLogService, never()).createOrderLog(any());
    }

    @Test
    void deleteEvidence_checksTenantOrderAndWorker() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        DeltaOrderEvidenceDO evidence = DeltaOrderEvidenceDO.builder().id(9L)
                .serviceOrderId(100L).workerId(5L).build();
        evidence.setTenantId(40L);
        when(evidenceService.getEvidence(9L)).thenReturn(evidence);
        service.doDeleteEvidence(context, 9L);
        verify(evidenceService).deleteEvidence(9L);
        verify(orderLogService).createOrderLog(any(DeltaOrderLogDO.class));

        evidence.setTenantId(99L);
        assertServiceException(() -> service.doDeleteEvidence(context, 9L),
                WORKER_ORDER_EVIDENCE_TENANT_MISMATCH);
    }

    @Test
    void deleteEvidence_rejectsAfterSubmitted() {
        DeltaWorkerOrderAccessContext context = context(60);
        when(accessService.revalidate(context)).thenReturn(context);
        assertServiceException(() -> service.doDeleteEvidence(context, 9L),
                EVIDENCE_CANNOT_OPERATE_AFTER_COMPLETE);
    }

    @Test
    void submitCompletion_updates50To60WithEvidenceAndBuyerOutbox() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        when(evidenceService.getEvidenceListByServiceOrderId(100L)).thenReturn(
                Collections.singletonList(DeltaOrderEvidenceDO.builder().id(9L).build()));
        service.doSubmitCompletion(context, "完成");
        verify(serviceOrderMapper).updateStatusCas(eq(100L), eq(60), eq(50), any());
        ArgumentCaptor<DeltaOrderProgressDO> captor = ArgumentCaptor.forClass(DeltaOrderProgressDO.class);
        verify(progressService).createProgress(captor.capture());
        assertEquals(100, captor.getValue().getProgressPercent());
        verify(eventPublisher).publishToBuyer(argThat((DeltaEventPublishReq req) ->
                Long.valueOf(40L).equals(req.getTenantId())));
    }

    @Test
    void submitCompletion_rejectsWithoutSourceEvidence() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        when(evidenceService.getEvidenceListByServiceOrderId(100L)).thenReturn(Collections.emptyList());
        assertServiceException(() -> service.doSubmitCompletion(context, "完成"),
                EVIDENCE_NO_COMPLETION_EVIDENCE);
        verify(serviceOrderMapper, never()).updateStatusCas(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    void submitCompletion_logFailurePropagatesForTransactionRollback() {
        DeltaWorkerOrderAccessContext context = context(50);
        when(accessService.revalidate(context)).thenReturn(context);
        when(evidenceService.getEvidenceListByServiceOrderId(100L)).thenReturn(
                Collections.singletonList(DeltaOrderEvidenceDO.builder().id(9L).build()));
        doThrow(new IllegalStateException("log failed")).when(orderLogService)
                .createOrderLog(any(DeltaOrderLogDO.class));
        assertThrows(IllegalStateException.class,
                () -> service.doSubmitCompletion(context, "完成"));
        verify(eventPublisher, never()).publishToBuyer(any());
    }

    private static DeltaWorkerOrderAccessContext context(Integer status) {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L).displayName("打手A").build();
        worker.setTenantId(30L);
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L).serviceOrderNo("SO100")
                .buyerUserId(60L).assignedWorkerId(5L).dispatchMode(4).status(status).build();
        order.setTenantId(40L);
        return DeltaWorkerOrderAccessContext.builder().worker(worker).workerTenantId(30L)
                .order(order).sourceTenantId(40L).clubOrder(true).build();
    }

    private static AppDeltaWorkerOrderProgressCreateReqVO progressReq(Integer type) {
        AppDeltaWorkerOrderProgressCreateReqVO req = new AppDeltaWorkerOrderProgressCreateReqVO();
        req.setServiceOrderId(100L);
        req.setProgressType(type);
        req.setProgressPercent(50);
        req.setContent("进度");
        return req;
    }

    private static AppDeltaWorkerOrderEvidenceCreateReqVO evidenceReq() {
        AppDeltaWorkerOrderEvidenceCreateReqVO req = new AppDeltaWorkerOrderEvidenceCreateReqVO();
        req.setServiceOrderId(100L);
        req.setEvidenceType(1);
        req.setFileUrl("https://example.test/evidence.png");
        req.setDescription("测试凭证");
        return req;
    }
}
