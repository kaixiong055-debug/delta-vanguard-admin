package cn.iocoder.yudao.module.delta.service.settlement;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.settlement.DeltaWorkerSettlementDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.settlement.DeltaWorkerSettlementMapper;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import cn.iocoder.yudao.module.delta.service.order.DeltaClubOrderTenantContext;
import cn.iocoder.yudao.module.delta.service.order.DeltaClubOrderTenantResolver;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaWorkerSettlementPhase7CrossTenantTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaWorkerSettlementPhase7CoreService service;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaWorkerService workerService;
    @Mock private DeltaWorkerSettlementService settlementService;
    @Mock private DeltaWorkerSettlementLogService settlementLogService;
    @Mock private DeltaNoRedisDAO noRedisDAO;
    @Mock private DeltaEventPublisher eventPublisher;
    @Mock private DeltaClubOrderTenantResolver clubOrderTenantResolver;
    @Mock private DeltaWorkerSettlementMapper settlementMapper;

    @BeforeEach
    void setUp() {
        lenient().when(serviceOrderMapper.selectById(100L)).thenReturn(order(4, null));
        lenient().when(noRedisDAO.generateSettlementNo()).thenReturn("SET100");
        lenient().when(settlementService.getMapper()).thenReturn(settlementMapper);
        lenient().when(clubOrderTenantResolver.resolve(any(DeltaServiceOrderDO.class)))
                .thenReturn(clubContext());
    }

    @Test
    void createSettlement_fallsBackToClubTenantWorkerCommission() {
        DeltaWorkerSettlementDO result = service.createSettlementForCompletedOrder(100L);
        assertEquals(1500, result.getCommissionRate());
        assertEquals(1500, result.getPlatformFee());
        assertEquals(8500, result.getWorkerAmount());
        verify(settlementService).createSettlement(result);
        verify(clubOrderTenantResolver).resolve(any(DeltaServiceOrderDO.class));
    }

    @Test
    void createSettlement_prefersOrderCommissionSnapshotWithoutWorkerLookup() {
        when(serviceOrderMapper.selectById(100L)).thenReturn(order(4, 1200));
        DeltaWorkerSettlementDO result = service.createSettlementForCompletedOrder(100L);
        assertEquals(1200, result.getCommissionRate());
        verifyNoInteractions(clubOrderTenantResolver);
        verifyNoInteractions(workerService);
    }

    @Test
    void approveSettlement_publishesClubWorkerOutboxInClubTenant() {
        DeltaWorkerSettlementDO settlement = DeltaWorkerSettlementDO.builder().id(7L)
                .settlementNo("SET100").serviceOrderId(100L).workerId(5L)
                .workerAmount(8500).settlementStatus(0).build();
        settlement.setTenantId(40L);
        when(settlementService.getSettlement(7L)).thenReturn(settlement);
        when(settlementMapper.updateStatusCas(eq(7L), eq(1), eq(0), any())).thenReturn(1);
        service.approveSettlement(9L, 7L, "通过");
        ArgumentCaptor<DeltaEventPublishReq> captor = ArgumentCaptor.forClass(DeltaEventPublishReq.class);
        verify(eventPublisher).publishToWorker(captor.capture());
        assertEquals(30L, captor.getValue().getTenantId());
        assertEquals(50L, captor.getValue().getRecipientId());
    }

    private static DeltaServiceOrderDO order(Integer dispatchMode, Integer commissionRate) {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L).serviceOrderNo("SO100")
                .assignedWorkerId(5L).dispatchMode(dispatchMode).status(80)
                .serviceAmount(10000).commissionRate(commissionRate).build();
        order.setTenantId(40L);
        return order;
    }

    private static DeltaClubOrderTenantContext clubContext() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L).commissionRate(1500).build();
        worker.setTenantId(30L);
        return DeltaClubOrderTenantContext.builder().order(order(4, null)).worker(worker)
                .sourceTenantId(40L).workerTenantId(30L).build();
    }
}
