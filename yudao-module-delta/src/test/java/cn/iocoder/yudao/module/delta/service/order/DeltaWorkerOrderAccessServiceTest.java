package cn.iocoder.yudao.module.delta.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.SERVICE_ORDER_NOT_BELONG_TO_WORKER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaWorkerOrderAccessServiceTest extends BaseMockitoUnitTest {

    @InjectMocks private DeltaWorkerOrderAccessService service;
    @Mock private DeltaWorkerService workerService;
    @Mock private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock private DeltaClubProfileMapper clubProfileMapper;
    @Mock private DeltaClubOrderTenantResolver clubOrderTenantResolver;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(30L);
        lenient().when(workerService.getWorkerByUserId(50L)).thenReturn(worker());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void resolve_supportsOrdinarySameTenantOrder() {
        DeltaServiceOrderDO order = localOrder();
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        DeltaWorkerOrderAccessContext context = service.resolve(50L, 100L);
        assertFalse(context.isClubOrder());
        assertEquals(30L, context.getSourceTenantId());
        assertSame(order, context.getOrder());
        verifyNoInteractions(clubOrderTenantResolver);
    }

    @Test
    void resolve_supportsTrustedClubOrder() {
        when(serviceOrderMapper.selectById(100L)).thenReturn(null);
        when(clubOrderTenantResolver.resolve(100L)).thenReturn(clubContext());
        DeltaWorkerOrderAccessContext context = service.resolve(50L, 100L);
        assertTrue(context.isClubOrder());
        assertEquals(40L, context.getSourceTenantId());
        assertEquals(30L, context.getWorkerTenantId());
    }

    @Test
    void resolve_rejectsOrderAssignedToAnotherWorker() {
        DeltaServiceOrderDO order = localOrder();
        order.setAssignedWorkerId(8L);
        when(serviceOrderMapper.selectById(100L)).thenReturn(order);
        assertServiceException(() -> service.resolve(50L, 100L),
                SERVICE_ORDER_NOT_BELONG_TO_WORKER);
    }

    @Test
    void pageDelegatesOneDatabasePageQueryWithClubBridge() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder().id(20L).businessStatus(1).build();
        club.setTenantId(30L);
        when(clubProfileMapper.selectByTenantId(30L)).thenReturn(club);
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(1);
        pageParam.setPageSize(10);
        PageResult<DeltaServiceOrderDO> expected = new PageResult<>(
                Collections.singletonList(localOrder()), 1L);
        when(serviceOrderMapper.selectWorkerAccessiblePage(pageParam, 5L, 30L, 20L, 50))
                .thenReturn(expected);
        assertSame(expected, service.getAccessiblePage(50L, 50, pageParam));
        verify(serviceOrderMapper, times(1))
                .selectWorkerAccessiblePage(pageParam, 5L, 30L, 20L, 50);
    }

    private static DeltaWorkerDO worker() {
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).userId(50L)
                .auditStatus(2).status(0).workStatus(2).build();
        worker.setTenantId(30L);
        return worker;
    }

    private static DeltaServiceOrderDO localOrder() {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L)
                .assignedWorkerId(5L).status(50).build();
        order.setTenantId(30L);
        return order;
    }

    private static DeltaClubOrderTenantContext clubContext() {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L)
                .assignedWorkerId(5L).dispatchMode(4).status(50).build();
        order.setTenantId(40L);
        return DeltaClubOrderTenantContext.builder().worker(worker()).order(order)
                .sourceTenantId(40L).workerTenantId(30L).build();
    }
}
