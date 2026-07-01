package cn.iocoder.yudao.module.delta.service.market;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CLUB_DISABLED;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_CLUB_NOT_EXISTS;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_EXPIRED;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_SERVICE_SCOPE_NOT_MATCH;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.ORDER_MARKET_STATUS_NOT_ALLOWED;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DeltaOrderMarketEligibilityServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DeltaOrderMarketEligibilityService service;
    @Mock
    private DeltaClubProfileMapper clubProfileMapper;
    @Mock
    private DeltaClubServiceScopeMapper serviceScopeMapper;
    @Mock
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Mock
    private DeltaOrderMarketListingMapper listingMapper;

    @Test
    void getAndValidateClubByOwnerMemberId_enabled() {
        DeltaClubProfileDO club = DeltaClubProfileDO.builder().businessStatus(1).build();
        when(clubProfileMapper.selectByOwnerMemberId(10L)).thenReturn(club);
        assertSame(club, service.getAndValidateClubByOwnerMemberId(10L));
    }

    @Test
    void getAndValidateClubByOwnerMemberId_notExists() {
        assertServiceException(() -> service.getAndValidateClubByOwnerMemberId(10L),
                ORDER_MARKET_CLUB_NOT_EXISTS);
    }

    @Test
    void getAndValidateClubByOwnerMemberId_disabled() {
        when(clubProfileMapper.selectByOwnerMemberId(10L)).thenReturn(
                DeltaClubProfileDO.builder().businessStatus(0).build());
        assertServiceException(() -> service.getAndValidateClubByOwnerMemberId(10L),
                ORDER_MARKET_CLUB_DISABLED);
    }

    @Test
    void checkEligibility_rejectsExpiredListing() {
        DeltaOrderMarketListingDO listing = createAvailableListing();
        listing.setExpireTime(LocalDateTime.now().minusSeconds(1));

        assertServiceException(() -> service.checkEligibility(listing, 20L, 30L),
                ORDER_MARKET_EXPIRED);
        verifyNoInteractions(serviceOrderMapper, serviceScopeMapper);
    }

    @Test
    void checkEligibility_rejectsClaimedListing() {
        DeltaOrderMarketListingDO listing = createAvailableListing();
        listing.setListingStatus(1);

        assertServiceException(() -> service.checkEligibility(listing, 20L, 30L),
                ORDER_MARKET_STATUS_NOT_ALLOWED);
        verifyNoInteractions(serviceOrderMapper, serviceScopeMapper);
    }

    @Test
    void checkEligibility_rejectsServiceScopeMismatch() {
        DeltaOrderMarketListingDO listing = createAvailableListing();
        when(serviceOrderMapper.selectById(100L)).thenReturn(
                DeltaServiceOrderDO.builder().status(10).build());
        when(clubProfileMapper.selectById(20L)).thenReturn(
                DeltaClubProfileDO.builder().id(20L).businessStatus(1).build());
        when(serviceScopeMapper.selectListByClubProfileId(20L)).thenReturn(
                Collections.singletonList(DeltaClubServiceScopeDO.builder()
                        .serviceType(2).enabled(true).build()));

        assertServiceException(() -> service.checkEligibility(listing, 20L, 30L),
                ORDER_MARKET_SERVICE_SCOPE_NOT_MATCH);
    }

    private static DeltaOrderMarketListingDO createAvailableListing() {
        return DeltaOrderMarketListingDO.builder()
                .id(1L)
                .serviceOrderId(100L)
                .serviceType(1)
                .listingStatus(0)
                .expireTime(LocalDateTime.now().plusHours(1))
                .build();
    }
}
