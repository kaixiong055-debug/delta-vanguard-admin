package cn.iocoder.yudao.module.delta.service.cluborder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAvailableWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAssignWorkerReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaServiceOrderLockRedisDAO;
import cn.iocoder.yudao.module.delta.service.market.DeltaOrderMarketEligibilityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ASSIGNMENT_ORDER_BEING_PROCESSED;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.CLUB_ORDER_ASSIGN_BUSY;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.CLUB_ORDER_NOT_BELONG_TO_CLUB;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.CLUB_ORDER_SOURCE_TENANT_MISMATCH;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CLUB_DISABLED;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CLUB_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class DeltaClubOrderServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DeltaClubOrderServiceImpl service;
    @Mock
    private DeltaOrderMarketEligibilityService eligibilityService;
    @Mock
    private DeltaOrderMarketListingMapper listingMapper;
    @Mock
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock
    private DeltaWorkerMapper workerMapper;
    @Mock
    private DeltaServiceOrderLockRedisDAO serviceOrderLockRedisDAO;
    @Mock
    private DeltaClubOrderAssignmentCoreService assignmentCoreService;

    @Test
    void getPageForMember_usesClubIdAndTenantAndBatchLoadsOrders() {
        DeltaClubProfileDO club = createClub();
        DeltaOrderMarketListingDO listing = createListing();
        DeltaServiceOrderDO order = createOrder();
        AppDeltaClubOrderPageReqVO reqVO = new AppDeltaClubOrderPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(club);
        when(listingMapper.selectClaimedPage(1, 10, 20L, 30L))
                .thenReturn(new PageResult<>(Collections.singletonList(listing), 1L));
        when(serviceOrderMapper.selectBatchIds(Collections.singletonList(100L)))
                .thenReturn(Collections.singletonList(order));

        PageResult<AppDeltaClubOrderPageRespVO> result = service.getPageForMember(10L, reqVO);

        assertEquals(1, result.getList().size());
        assertEquals(100L, result.getList().get(0).getServiceOrderId());
        verify(listingMapper).selectClaimedPage(1, 10, 20L, 30L);
        verify(serviceOrderMapper).selectBatchIds(Collections.singletonList(100L));
    }

    @Test
    void getDetailForMember_rejectsOtherClubListing() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        DeltaOrderMarketListingDO listing = createListing();
        listing.setClaimedClubId(99L);
        when(listingMapper.selectById(1L)).thenReturn(listing);

        assertServiceException(() -> service.getDetailForMember(10L, 1L),
                CLUB_ORDER_NOT_BELONG_TO_CLUB);
    }

    @Test
    void getDetailForMember_rejectsOrdinaryMember() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L))
                .thenThrow(exception(ORDER_MARKET_CLUB_NOT_EXISTS));
        assertServiceException(() -> service.getDetailForMember(10L, 1L),
                ORDER_MARKET_CLUB_NOT_EXISTS);
    }

    @Test
    void getDetailForMember_rejectsDisabledClub() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L))
                .thenThrow(exception(ORDER_MARKET_CLUB_DISABLED));
        assertServiceException(() -> service.getDetailForMember(10L, 1L),
                ORDER_MARKET_CLUB_DISABLED);
    }

    @Test
    void getDetailForMember_rejectsSourceTenantMismatch() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(1L)).thenReturn(createListing());
        DeltaServiceOrderDO foreignOrder = createOrder();
        foreignOrder.setTenantId(999L);
        when(serviceOrderMapper.selectById(100L)).thenReturn(null, foreignOrder);

        assertServiceException(() -> service.getDetailForMember(10L, 1L),
                CLUB_ORDER_SOURCE_TENANT_MISMATCH);
    }

    @Test
    void getAvailableWorkerPage_filtersInDatabaseWithGlobalBusyWorkers() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(1L)).thenReturn(createListing());
        when(serviceOrderMapper.selectById(100L)).thenReturn(createOrder());
        when(serviceOrderMapper.selectActiveAssignedWorkerIds())
                .thenReturn(new HashSet<>(Collections.singletonList(88L)));
        DeltaWorkerDO worker = DeltaWorkerDO.builder().id(5L).displayName("打手A")
                .workStatus(1).build();
        worker.setTenantId(30L);
        when(workerMapper.selectClubAvailablePage(any(), eq(30L), eq(1), eq(3), anySet()))
                .thenReturn(new PageResult<>(Collections.singletonList(worker), 1L));
        AppDeltaClubOrderAvailableWorkerPageReqVO reqVO =
                new AppDeltaClubOrderAvailableWorkerPageReqVO();
        reqVO.setListingId(1L);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppDeltaClubOrderWorkerRespVO> result =
                service.getAvailableWorkerPageForMember(10L, reqVO);

        assertEquals("打手A", result.getList().get(0).getDisplayName());
        verify(workerMapper).selectClubAvailablePage(eq(reqVO), eq(30L), eq(1), eq(3),
                eq(new HashSet<>(Collections.singletonList(88L))));
    }

    @Test
    void getDetailForMember_returnsSafeOwnedDetail() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(1L)).thenReturn(createListing());
        when(serviceOrderMapper.selectById(100L)).thenReturn(createOrder());

        AppDeltaClubOrderDetailRespVO result = service.getDetailForMember(10L, 1L);

        assertEquals("SO100", result.getServiceOrderNo());
        assertEquals("客户安全备注", result.getCustomerRemark());
    }

    @Test
    void assignWorkerForMember_usesTrustedSourceTenantLockAndCore() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(1L)).thenReturn(createListing());
        when(serviceOrderMapper.selectById(100L)).thenReturn(createOrder());
        when(serviceOrderLockRedisDAO.lockAndRun(eq(40L), eq(100L), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(2)).get());
        AppDeltaClubOrderAssignWorkerReqVO reqVO = new AppDeltaClubOrderAssignWorkerReqVO();
        reqVO.setListingId(1L);
        reqVO.setWorkerId(5L);
        reqVO.setRemark("安全备注");

        service.assignWorkerForMember(10L, reqVO);

        verify(assignmentCoreService).assign(1L, 20L, 30L, 10L, 5L, "安全备注");
    }

    @Test
    void assignWorkerForMember_mapsLockBusyError() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(1L)).thenReturn(createListing());
        when(serviceOrderMapper.selectById(100L)).thenReturn(createOrder());
        doThrow(exception(ASSIGNMENT_ORDER_BEING_PROCESSED)).when(serviceOrderLockRedisDAO)
                .lockAndRun(eq(40L), eq(100L), any());
        AppDeltaClubOrderAssignWorkerReqVO reqVO = new AppDeltaClubOrderAssignWorkerReqVO();
        reqVO.setListingId(1L);
        reqVO.setWorkerId(5L);

        assertServiceException(() -> service.assignWorkerForMember(10L, reqVO),
                CLUB_ORDER_ASSIGN_BUSY);
    }

    private static DeltaClubProfileDO createClub() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder()
                .id(20L).ownerMemberId(10L).businessStatus(1).build();
        club.setTenantId(30L);
        return club;
    }

    private static DeltaOrderMarketListingDO createListing() {
        return DeltaOrderMarketListingDO.builder().id(1L).listingNo("L1")
                .serviceOrderId(100L).sourceTenantId(40L).serviceType(1)
                .claimedClubId(20L).claimedClubTenantId(30L).listingStatus(1).build();
    }

    private static DeltaServiceOrderDO createOrder() {
        DeltaServiceOrderDO order = DeltaServiceOrderDO.builder().id(100L)
                .serviceOrderNo("SO100").serviceType(1).deviceType(3)
                .serviceAmount(5000).status(10).customerRemark("客户安全备注").build();
        order.setTenantId(40L);
        return order;
    }
}
