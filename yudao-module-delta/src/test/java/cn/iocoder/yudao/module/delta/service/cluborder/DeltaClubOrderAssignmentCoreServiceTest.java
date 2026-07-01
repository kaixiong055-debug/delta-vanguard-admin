package cn.iocoder.yudao.module.delta.service.cluborder;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAssignmentMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerSkillMapper;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.DispatchModeEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaClubOrderAssignmentCoreServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DeltaClubOrderAssignmentCoreService service;
    @Mock private DeltaOrderMarketListingMapper listingMapper;
    @Mock private DeltaClubProfileMapper clubProfileMapper;
    @Mock private DeltaOrderMarketLogMapper marketLogMapper;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaOrderAssignmentMapper assignmentMapper;
    @Mock private DeltaOrderLogMapper orderLogMapper;
    @Mock private DeltaWorkerMapper workerMapper;
    @Mock private DeltaWorkerSkillMapper workerSkillMapper;
    @Mock private DeltaEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        lenient().when(listingMapper.selectById(1L)).thenReturn(createListing());
        lenient().when(clubProfileMapper.selectById(20L)).thenReturn(createClub());
        lenient().when(serviceOrderMapper.selectById(100L)).thenReturn(createOrder());
        lenient().when(workerMapper.selectById(5L)).thenReturn(createWorker());
        lenient().when(workerSkillMapper.hasMatchingSkill(5L, 3, 1)).thenReturn(true);
        lenient().when(serviceOrderMapper.existsActiveByWorkerId(5L)).thenReturn(false);
        lenient().when(serviceOrderMapper.updateClubAssignCas(eq(100L), eq(5L), eq(4),
                any(LocalDateTime.class))).thenReturn(1);
        lenient().when(workerMapper.updateWorkStatusCas(5L, 2, 1)).thenReturn(1);
    }

    @Test
    void assign_successWritesOrderAssignmentLogsWorkerAndOutbox() throws Exception {
        service.assign(1L, 20L, 30L, 10L, 5L, "安全备注");

        verify(serviceOrderMapper).updateClubAssignCas(eq(100L), eq(5L),
                eq(DispatchModeEnum.CLUB_ASSIGN.getMode()), any(LocalDateTime.class));
        ArgumentCaptor<DeltaOrderAssignmentDO> assignmentCaptor =
                ArgumentCaptor.forClass(DeltaOrderAssignmentDO.class);
        verify(assignmentMapper).insert(assignmentCaptor.capture());
        assertEquals(AssignmentTypeEnum.CLUB_ASSIGN.getType(),
                assignmentCaptor.getValue().getAssignmentType());
        assertEquals(AssignmentStatusEnum.ACCEPTED.getStatus(),
                assignmentCaptor.getValue().getAssignmentStatus());
        assertEquals(OperatorTypeEnum.CLUB_OWNER.getType(),
                assignmentCaptor.getValue().getOperatorType());
        verify(workerMapper).updateWorkStatusCas(5L, 2, 1);
        verify(orderLogMapper).insert(argThat((DeltaOrderLogDO log) -> isClubOrderLog(log)));
        verify(marketLogMapper).insert(argThat((DeltaOrderMarketLogDO log) -> isClubMarketLog(log)));
        verify(eventPublisher).publishToWorker(argThat(this::isAssignedEvent));
        verify(listingMapper, never()).updateById(any(DeltaOrderMarketListingDO.class));
        assertNotNull(DeltaClubOrderAssignmentCoreService.class.getMethod("assign",
                Long.class, Long.class, Long.class, Long.class, Long.class, String.class)
                .getAnnotation(Transactional.class));
    }

    @Test
    void assign_rejectsOtherTenantWorker() {
        DeltaWorkerDO foreignWorker = createWorker();
        foreignWorker.setTenantId(99L);
        when(workerMapper.selectById(5L)).thenReturn(null, foreignWorker);

        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_WORKER_TENANT_MISMATCH);
        verify(serviceOrderMapper, never()).updateClubAssignCas(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    void assign_rejectsDisabledClub() {
        DeltaClubProfileDO club = createClub();
        club.setBusinessStatus(0);
        when(clubProfileMapper.selectById(20L)).thenReturn(club);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                ORDER_MARKET_CLUB_DISABLED);
    }

    @Test
    void assign_rejectsClaimedClubTenantMismatch() {
        DeltaOrderMarketListingDO listing = createListing();
        listing.setClaimedClubTenantId(99L);
        when(listingMapper.selectById(1L)).thenReturn(listing);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_NOT_BELONG_TO_CLUB);
    }

    @Test
    void assign_rejectsNonAssignableOrderStatus() {
        DeltaServiceOrderDO order = createOrder();
        order.setStatus(40);
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_STATUS_NOT_ASSIGNABLE);
    }

    @Test
    void assign_rejectsAlreadyAssignedWorkerWithoutOverwrite() {
        DeltaServiceOrderDO order = createOrder();
        order.setAssignedWorkerId(8L);
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_ALREADY_ASSIGNED);
        verify(serviceOrderMapper, never()).updateClubAssignCas(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    void assign_rejectsOfflineWorker() {
        DeltaWorkerDO worker = createWorker();
        worker.setWorkStatus(0);
        when(workerMapper.selectById(5L)).thenReturn(worker);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_WORKER_NOT_AVAILABLE);
    }

    @Test
    void assign_rejectsWorkerWithActiveOrder() {
        when(serviceOrderMapper.existsActiveByWorkerId(5L)).thenReturn(true);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_WORKER_HAS_ACTIVE_ORDER);
    }

    @Test
    void assign_rejectsSkillMismatch() {
        when(workerSkillMapper.hasMatchingSkill(5L, 3, 1)).thenReturn(false);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_WORKER_SKILL_NOT_MATCH);
    }

    @Test
    void assign_rejectsDuplicateAssignment() {
        when(assignmentMapper.selectAnyActiveByServiceOrderId(100L))
                .thenReturn(DeltaOrderAssignmentDO.builder().id(9L).build());
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_ALREADY_ASSIGNED);
    }

    @Test
    void assign_workerCasFailurePropagatesBeforeLogsAndOutbox() {
        when(workerMapper.updateWorkStatusCas(5L, 2, 1)).thenReturn(0);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_ASSIGN_STATUS_CHANGED);
        verify(marketLogMapper, never()).insert(any(DeltaOrderMarketLogDO.class));
        verify(eventPublisher, never()).publishToWorker(any());
    }

    @Test
    void assign_orderCasFailureCreatesNoPartialRecords() {
        when(serviceOrderMapper.updateClubAssignCas(eq(100L), eq(5L), eq(4), any()))
                .thenReturn(0);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_ASSIGN_STATUS_CHANGED);
        verify(assignmentMapper, never()).insert(any(DeltaOrderAssignmentDO.class));
        verify(orderLogMapper, never()).insert(any(DeltaOrderLogDO.class));
        verify(workerMapper, never()).updateWorkStatusCas(anyLong(), anyInt(), anyInt());
    }

    @Test
    void assign_competingCasOnlyFirstRequestSucceeds() {
        when(serviceOrderMapper.updateClubAssignCas(eq(100L), eq(5L), eq(4), any()))
                .thenReturn(1, 0);
        service.assign(1L, 20L, 30L, 10L, 5L, null);
        assertServiceException(() -> service.assign(1L, 20L, 30L, 10L, 5L, null),
                CLUB_ORDER_ASSIGN_STATUS_CHANGED);
        verify(eventPublisher, times(1)).publishToWorker(any());
    }

    @Test
    void assign_assignmentInsertFailurePropagatesForTransactionRollback() {
        doThrow(new IllegalStateException("insert failed")).when(assignmentMapper)
                .insert(any(DeltaOrderAssignmentDO.class));
        assertThrows(IllegalStateException.class,
                () -> service.assign(1L, 20L, 30L, 10L, 5L, null));
        verify(workerMapper, never()).updateWorkStatusCas(anyLong(), anyInt(), anyInt());
        verify(eventPublisher, never()).publishToWorker(any());
    }

    @Test
    void assign_outboxFailurePropagatesForTransactionRollback() {
        doThrow(new IllegalStateException("outbox failed")).when(eventPublisher).publishToWorker(any());
        assertThrows(IllegalStateException.class,
                () -> service.assign(1L, 20L, 30L, 10L, 5L, null));
    }

    private boolean isClubOrderLog(DeltaOrderLogDO log) {
        return "俱乐部分派打手".equals(log.getOperation())
                && OperatorTypeEnum.CLUB_OWNER.getType().equals(log.getOperatorType())
                && Integer.valueOf(10).equals(log.getBeforeStatus())
                && Integer.valueOf(40).equals(log.getAfterStatus());
    }

    private boolean isClubMarketLog(DeltaOrderMarketLogDO log) {
        return DeltaOrderMarketOperationTypeEnum.ASSIGN_WORKER.getType().equals(log.getOperationType())
                && Long.valueOf(20L).equals(log.getClubId())
                && Long.valueOf(30L).equals(log.getClubTenantId())
                && Integer.valueOf(1).equals(log.getBeforeStatus())
                && Integer.valueOf(1).equals(log.getAfterStatus());
    }

    private boolean isAssignedEvent(DeltaEventPublishReq req) {
        return DeltaEventTypeEnum.CLUB_ORDER_WORKER_ASSIGNED.getType().equals(req.getEventType())
                && Long.valueOf(30L).equals(req.getTenantId())
                && Long.valueOf(5L).equals(req.getPayload().getWorkerId())
                && Long.valueOf(50L).equals(req.getRecipientId());
    }

    private static DeltaOrderMarketListingDO createListing() {
        return DeltaOrderMarketListingDO.builder().id(1L).listingNo("L1")
                .serviceOrderId(100L).sourceTenantId(40L)
                .claimedClubId(20L).claimedClubTenantId(30L).listingStatus(1).build();
    }

    private static DeltaClubProfileDO createClub() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder().id(20L).ownerMemberId(10L)
                .businessStatus(1).build();
        club.setTenantId(30L);
        return club;
    }

    private static DeltaServiceOrderDO createOrder() {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L)
                .serviceOrderNo("SO100").serviceType(1).deviceType(3).status(10).build();
        order.setTenantId(40L);
        return order;
    }

    private static DeltaWorkerDO createWorker() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L)
                .auditStatus(2).status(0).workStatus(1).build();
        worker.setTenantId(30L);
        return worker;
    }
}
