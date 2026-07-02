package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.vo.AppDeltaWorkerOrderProgressCreateReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderProgressDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaServiceOrderLockRedisDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaServiceOrderWorkerCrossTenantTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaServiceOrderServiceImpl service;
    @Mock private DeltaWorkerOrderAccessService accessService;
    @Mock private DeltaWorkerOrderExecutionCoreService executionCoreService;
    @Mock private DeltaServiceOrderLockRedisDAO lockRedisDAO;
    @Mock private DeltaOrderEvidenceService evidenceService;

    @BeforeEach
    void setUp() {
        lenient().when(accessService.resolve(50L, 100L)).thenReturn(context());
        lenient().when(lockRedisDAO.lockAndRun(eq(40L), eq(100L), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(2)).get());
    }

    @Test
    void start_usesSourceTenantLockAndCore() {
        service.startService(50L, 100L);
        verify(lockRedisDAO).lockAndRun(eq(40L), eq(100L), any());
        verify(executionCoreService).doStartService(any(DeltaWorkerOrderAccessContext.class));
    }

    @Test
    void progress_switchesToSourceTenantCore() {
        AppDeltaWorkerOrderProgressCreateReqVO req = new AppDeltaWorkerOrderProgressCreateReqVO();
        req.setServiceOrderId(100L);
        DeltaOrderProgressDO expected = DeltaOrderProgressDO.builder().id(7L).build();
        when(executionCoreService.doCreateProgress(any(), same(req))).thenReturn(expected);
        assertSame(expected, service.createProgress(50L, req));
    }

    @Test
    void deleteEvidence_resolvesGloballyThenRevalidatesOrderTenantAndWorker() {
        DeltaOrderEvidenceDO evidence = DeltaOrderEvidenceDO.builder().id(9L)
                .serviceOrderId(100L).workerId(5L).build();
        evidence.setTenantId(40L);
        when(evidenceService.getEvidence(9L)).thenReturn(evidence);
        service.deleteEvidence(50L, 9L);
        verify(accessService).resolve(50L, 100L);
        verify(executionCoreService).doDeleteEvidence(any(DeltaWorkerOrderAccessContext.class), eq(9L));
    }

    @Test
    void submitCompletion_usesSourceTenantLock() {
        service.submitCompletion(50L, 100L, "完成");
        verify(lockRedisDAO).lockAndRun(eq(40L), eq(100L), any());
        verify(executionCoreService).doSubmitCompletion(any(DeltaWorkerOrderAccessContext.class), eq("完成"));
    }

    private static DeltaWorkerOrderAccessContext context() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L).build();
        worker.setTenantId(30L);
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L)
                .assignedWorkerId(5L).dispatchMode(4).build();
        order.setTenantId(40L);
        return DeltaWorkerOrderAccessContext.builder().worker(worker).workerTenantId(30L)
                .order(order).sourceTenantId(40L).clubOrder(true).build();
    }
}
