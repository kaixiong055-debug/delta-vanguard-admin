package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAssignmentMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeltaClubOrderTenantResolverTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaClubOrderTenantResolver resolver;
    @Mock private DeltaOrderMarketListingMapper listingMapper;
    @Mock private DeltaClubProfileMapper clubProfileMapper;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaOrderAssignmentMapper assignmentMapper;
    @Mock private DeltaWorkerMapper workerMapper;

    @BeforeEach
    void setUp() {
        lenient().when(listingMapper.selectClaimedByServiceOrderId(100L)).thenReturn(listing());
        lenient().when(serviceOrderMapper.selectById(100L)).thenReturn(order());
        lenient().when(clubProfileMapper.selectById(20L)).thenReturn(club());
        lenient().when(workerMapper.selectById(5L)).thenReturn(worker());
        lenient().when(assignmentMapper.selectActiveByServiceOrderId(100L)).thenReturn(assignment());
    }

    @Test
    void resolve_returnsTrustedCrossTenantContext() {
        DeltaClubOrderTenantContext context = resolver.resolve(100L);
        assertEquals(40L, context.getSourceTenantId());
        assertEquals(30L, context.getWorkerTenantId());
        assertEquals(20L, context.getClub().getId());
        assertEquals(5L, context.getWorker().getId());
    }

    @Test
    void resolve_rejectsMissingClaimedListing() {
        when(listingMapper.selectClaimedByServiceOrderId(100L)).thenReturn(null);
        assertServiceException(() -> resolver.resolve(100L),
                WORKER_ORDER_CROSS_TENANT_CONTEXT_NOT_FOUND);
    }

    @Test
    void resolve_rejectsSourceTenantMismatch() {
        DeltaServiceOrderDO order = order();
        order.setTenantId(99L);
        assertServiceException(() -> resolver.resolve(order), CLUB_ORDER_SOURCE_TENANT_MISMATCH);
    }

    @Test
    void resolve_rejectsDisabledClub() {
        DeltaClubProfileDO club = club();
        club.setBusinessStatus(0);
        when(clubProfileMapper.selectById(20L)).thenReturn(club);
        assertServiceException(() -> resolver.resolve(100L), WORKER_ORDER_CLUB_MISMATCH);
    }

    @Test
    void resolve_rejectsNonClubDispatchMode() {
        DeltaServiceOrderDO order = order();
        order.setDispatchMode(2);
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        assertServiceException(() -> resolver.resolve(100L), WORKER_ORDER_ASSIGNMENT_MISMATCH);
    }

    @Test
    void resolve_rejectsWrongAssignmentWorkerOrType() {
        DeltaOrderAssignmentDO assignment = assignment();
        assignment.setWorkerId(8L);
        when(assignmentMapper.selectActiveByServiceOrderId(100L)).thenReturn(assignment);
        assertServiceException(() -> resolver.resolve(100L), WORKER_ORDER_ASSIGNMENT_MISMATCH);
    }

    private static DeltaOrderMarketListingDO listing() {
        return DeltaOrderMarketListingDO.builder().id(1L).serviceOrderId(100L)
                .sourceTenantId(40L).claimedClubId(20L).claimedClubTenantId(30L)
                .listingStatus(1).build();
    }

    private static DeltaServiceOrderDO order() {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L).assignedWorkerId(5L)
                .dispatchMode(4).status(40).build();
        order.setTenantId(40L);
        return order;
    }

    private static DeltaClubProfileDO club() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder().id(20L).businessStatus(1).build();
        club.setTenantId(30L);
        return club;
    }

    private static DeltaWorkerDO worker() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L).build();
        worker.setTenantId(30L);
        return worker;
    }

    private static DeltaOrderAssignmentDO assignment() {
        return DeltaOrderAssignmentDO.builder().id(9L).serviceOrderId(100L).workerId(5L)
                .assignmentType(5).assignmentStatus(2).build();
    }
}
