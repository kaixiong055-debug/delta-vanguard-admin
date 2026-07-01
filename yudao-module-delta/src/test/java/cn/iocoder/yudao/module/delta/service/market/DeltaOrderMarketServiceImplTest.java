package cn.iocoder.yudao.module.delta.service.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketLogMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaOrderMarketLockRedisDAO;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CAS_FAILED;
import static cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum.CLAIM;
import static cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum.AVAILABLE;
import static cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum.CLAIMED;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeltaOrderMarketServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DeltaOrderMarketServiceImpl service;
    @Mock
    private DeltaOrderMarketListingMapper listingMapper;
    @Mock
    private DeltaOrderMarketLogMapper logMapper;
    @Mock
    private DeltaClubServiceScopeMapper serviceScopeMapper;
    @Mock
    private DeltaOrderMarketLockRedisDAO lockRedisDAO;
    @Mock
    private DeltaEventPublisher eventPublisher;
    @Mock
    private DeltaOrderMarketEligibilityService eligibilityService;

    @Test
    void getAvailablePageForMember_filtersByEnabledServiceScopes() {
        DeltaClubProfileDO club = createClub();
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(club);
        when(serviceScopeMapper.selectListByClubProfileId(300L)).thenReturn(Arrays.asList(
                DeltaClubServiceScopeDO.builder().serviceType(1).enabled(true).build(),
                DeltaClubServiceScopeDO.builder().serviceType(2).enabled(false).build(),
                DeltaClubServiceScopeDO.builder().serviceType(3).enabled(true).build()));
        PageResult<DeltaOrderMarketListingDO> expected =
                new PageResult<>(Collections.<DeltaOrderMarketListingDO>emptyList(), 0L);
        when(listingMapper.selectAvailablePageByServiceTypes(
                eq(1), eq(20), any(LocalDateTime.class),
                eq(new HashSet<>(Arrays.asList(1, 3))))).thenReturn(expected);

        PageResult<DeltaOrderMarketListingDO> actual =
                service.getAvailablePageForMember(10L, 1, 20);

        assertSame(expected, actual);
    }

    @Test
    void getAvailablePageForMember_returnsEmptyWhenNoEnabledScope() {
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(serviceScopeMapper.selectListByClubProfileId(300L)).thenReturn(Collections.singletonList(
                DeltaClubServiceScopeDO.builder().serviceType(1).enabled(false).build()));

        PageResult<DeltaOrderMarketListingDO> result =
                service.getAvailablePageForMember(10L, 1, 20);

        verify(listingMapper, never()).selectAvailablePageByServiceTypes(
                anyInt(), anyInt(), any(LocalDateTime.class), anySet());
        org.junit.jupiter.api.Assertions.assertTrue(result.getList().isEmpty());
    }

    @Test
    void getAvailableForMember_usesOwnerClubForEligibility() {
        DeltaClubProfileDO club = createClub();
        DeltaOrderMarketListingDO listing = DeltaOrderMarketListingDO.builder().id(100L).build();
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(club);
        when(listingMapper.selectById(100L)).thenReturn(listing);

        assertSame(listing, service.getAvailableForMember(10L, 100L));
        verify(eligibilityService).checkEligibility(listing, 300L, 400L);
    }

    @Test
    void getMyClaimedPageForMember_usesTrustedClubTenantId() {
        PageResult<DeltaOrderMarketListingDO> expected =
                new PageResult<>(Collections.<DeltaOrderMarketListingDO>emptyList(), 0L);
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectClaimedPage(1, 20, 400L)).thenReturn(expected);

        assertSame(expected, service.getMyClaimedPageForMember(10L, 1, 20));
        verify(listingMapper).selectClaimedPage(1, 20, 400L);
    }

    @Test
    void claimForMember_preservesLockCasLogAndOutbox() {
        DeltaClubProfileDO club = createClub();
        DeltaOrderMarketListingDO listing = createAvailableListing();
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(club);
        when(listingMapper.selectById(100L)).thenReturn(listing);
        when(listingMapper.updateClaimCas(eq(100L), eq(0), eq(300L), eq(400L),
                any(LocalDateTime.class))).thenReturn(1);
        when(lockRedisDAO.lockAndRun(eq(100L), any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        service.claimForMember(10L, 100L);

        verify(eligibilityService).checkEligibility(listing, 300L, 400L);
        verify(eligibilityService).recheckOrderStatus(200L);
        verify(eligibilityService).recheckClubCapacity(300L, 400L, 5);
        verify(listingMapper).updateClaimCas(eq(100L), eq(0), eq(300L), eq(400L),
                any(LocalDateTime.class));
        verify(logMapper).insert(argThat(log -> isSuccessfulClaimLog(log)));
        verify(eventPublisher).publishToAdmin(argThat(this::isClaimedEvent));
    }

    @Test
    void claimForMember_concurrentCasLoserFailsAndDoesNotPublishEvent() {
        DeltaOrderMarketListingDO listing = createAvailableListing();
        when(eligibilityService.getAndValidateClubByOwnerMemberId(10L)).thenReturn(createClub());
        when(listingMapper.selectById(100L)).thenReturn(listing);
        when(listingMapper.updateClaimCas(eq(100L), eq(0), eq(300L), eq(400L),
                any(LocalDateTime.class))).thenReturn(0);
        when(lockRedisDAO.lockAndRun(eq(100L), any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        assertServiceException(() -> service.claimForMember(10L, 100L), ORDER_MARKET_CAS_FAILED);

        verify(logMapper).insert(argThat(log -> Integer.valueOf(0).equals(log.getSuccess())));
        verify(eventPublisher, never()).publishToAdmin(any(DeltaEventPublishReq.class));
    }

    private static DeltaClubProfileDO createClub() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder()
                .id(300L)
                .businessStatus(1)
                .maxConcurrentOrders(5)
                .build();
        club.setTenantId(400L);
        return club;
    }

    private static DeltaOrderMarketListingDO createAvailableListing() {
        return DeltaOrderMarketListingDO.builder()
                .id(100L)
                .listingNo("LISTING-100")
                .serviceOrderId(200L)
                .sourceTenantId(500L)
                .listingStatus(AVAILABLE.getStatus())
                .expireTime(LocalDateTime.now().plusHours(1))
                .version(0)
                .build();
    }

    private static boolean isSuccessfulClaimLog(DeltaOrderMarketLogDO log) {
        return CLAIM.getType().equals(log.getOperationType())
                && Integer.valueOf(AVAILABLE.getStatus()).equals(log.getBeforeStatus())
                && Integer.valueOf(CLAIMED.getStatus()).equals(log.getAfterStatus())
                && Integer.valueOf(1).equals(log.getSuccess())
                && Long.valueOf(300L).equals(log.getClubId())
                && Long.valueOf(400L).equals(log.getClubTenantId());
    }

    private boolean isClaimedEvent(DeltaEventPublishReq req) {
        return Long.valueOf(100L).equals(req.getAggregateId())
                && Long.valueOf(500L).equals(req.getTenantId())
                && Long.valueOf(300L).equals(req.getRecipientId());
    }
}
