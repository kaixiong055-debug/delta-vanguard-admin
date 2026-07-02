package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderEvidenceDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import cn.iocoder.yudao.module.delta.service.settlement.DeltaWorkerSettlementPhase7CoreService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.WORKER_ORDER_WORKER_STATUS_CHANGED;

class DeltaServiceOrderPhase6CrossTenantTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaServiceOrderPhase6CoreService service;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaWorkerService workerService;
    @Mock private DeltaWorkerMapper workerMapper;
    @Mock private DeltaOrderAssignmentService assignmentService;
    @Mock private DeltaOrderLogService orderLogService;
    @Mock private DeltaOrderProgressService progressService;
    @Mock private DeltaOrderEvidenceService evidenceService;
    @Mock private DeltaOrderAcceptanceService acceptanceService;
    @Mock private DeltaOrderReworkService reworkService;
    @Mock private DeltaWorkerSettlementPhase7CoreService settlementCoreService;
    @Mock private DeltaEventPublisher eventPublisher;
    @Mock private DeltaClubOrderTenantResolver clubOrderTenantResolver;

    @BeforeEach
    void setUp() {
        lenient().when(serviceOrderMapper.selectById(100L)).thenReturn(order(4));
        lenient().when(assignmentService.getActiveAssignmentByServiceOrderId(100L))
                .thenReturn(DeltaOrderAssignmentDO.builder().id(9L).workerId(5L).build());
        lenient().when(evidenceService.getEvidenceListByServiceOrderId(100L))
                .thenReturn(Collections.singletonList(DeltaOrderEvidenceDO.builder().id(8L).build()));
        lenient().when(serviceOrderMapper.updateStatusCas(anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(1);
        lenient().when(workerService.getWorkerMapper()).thenReturn(workerMapper);
        lenient().when(workerMapper.updateWorkStatusCas(5L, 1, 2)).thenReturn(1);
        lenient().when(clubOrderTenantResolver.resolve(any(DeltaServiceOrderDO.class)))
                .thenReturn(clubContext());
    }

    @Test
    void accept_clubOrderReleasesBusyWorkerInClubTenantAndCreatesSettlement() {
        when(serviceOrderMapper.existsOtherActiveByWorkerId(5L, 100L)).thenReturn(false);
        service.doAcceptByBuyer(60L, 100L, "通过");
        verify(workerMapper).updateWorkStatusCas(5L, 1, 2);
        verify(settlementCoreService).createSettlementForCompletedOrder(100L);
        verify(eventPublisher).publishToWorker(argThat((DeltaEventPublishReq req) ->
                Long.valueOf(30L).equals(req.getTenantId())
                        && Long.valueOf(50L).equals(req.getRecipientId())));
    }

    @Test
    void accept_workerWithOtherActiveOrderStaysBusy() {
        when(serviceOrderMapper.existsOtherActiveByWorkerId(5L, 100L)).thenReturn(true);
        service.doAcceptByBuyer(60L, 100L, null);
        verify(workerMapper, never()).updateWorkStatusCas(anyLong(), anyInt(), anyInt());
        verify(settlementCoreService).createSettlementForCompletedOrder(100L);
    }

    @Test
    void accept_workerStatusCasFailureRollsBackByException() {
        when(serviceOrderMapper.existsOtherActiveByWorkerId(5L, 100L)).thenReturn(false);
        when(workerMapper.updateWorkStatusCas(5L, 1, 2)).thenReturn(0);
        assertServiceException(() -> service.doAcceptByBuyer(60L, 100L, null),
                WORKER_ORDER_WORKER_STATUS_CHANGED);
        verify(settlementCoreService, never()).createSettlementForCompletedOrder(anyLong());
    }

    @Test
    void accept_settlementFailurePropagatesForWholeTransactionRollback() {
        when(serviceOrderMapper.existsOtherActiveByWorkerId(5L, 100L)).thenReturn(false);
        doThrow(new IllegalStateException("settlement failed")).when(settlementCoreService)
                .createSettlementForCompletedOrder(100L);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> service.doAcceptByBuyer(60L, 100L, null));
        verify(eventPublisher, never()).publishToWorker(any());
    }

    @Test
    void requestRework_keepsBusyAndWritesWorkerNotificationToClubTenant() {
        when(reworkService.countReworkByServiceOrderId(100L)).thenReturn(0L);
        service.doRequestReworkByBuyer(60L, 100L, "补充凭证");
        verify(workerMapper, never()).updateWorkStatusCas(anyLong(), anyInt(), anyInt());
        verify(eventPublisher).publishToWorker(argThat((DeltaEventPublishReq req) ->
                Long.valueOf(30L).equals(req.getTenantId())
                        && Long.valueOf(50L).equals(req.getRecipientId())));
    }

    @Test
    void accept_ordinaryOrderKeepsSameTenantWorkerPath() {
        DeltaServiceOrderDO order = order(2);
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        DeltaWorkerDO worker = worker();
        worker.setTenantId(40L);
        when(workerService.getWorker(5L)).thenReturn(worker);
        when(serviceOrderMapper.existsOtherActiveByWorkerId(5L, 100L)).thenReturn(false);
        service.doAcceptByBuyer(60L, 100L, null);
        verifyNoInteractions(clubOrderTenantResolver);
        verify(workerMapper).updateWorkStatusCas(5L, 1, 2);
    }

    private static DeltaServiceOrderDO order(Integer dispatchMode) {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L).serviceOrderNo("SO100")
                .buyerUserId(60L).assignedWorkerId(5L).dispatchMode(dispatchMode)
                .status(60).serviceAmount(10000).commissionRate(1000).build();
        order.setTenantId(40L);
        return order;
    }

    private static DeltaWorkerDO worker() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L)
                .status(0).workStatus(2).commissionRate(1000).build();
        worker.setTenantId(30L);
        return worker;
    }

    private static DeltaClubOrderTenantContext clubContext() {
        return DeltaClubOrderTenantContext.builder().order(order(4)).worker(worker())
                .sourceTenantId(40L).workerTenantId(30L).build();
    }
}
