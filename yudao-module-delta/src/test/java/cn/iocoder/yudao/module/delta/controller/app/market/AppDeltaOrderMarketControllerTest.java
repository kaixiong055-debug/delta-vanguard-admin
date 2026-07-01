package cn.iocoder.yudao.module.delta.controller.app.market;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.club.AppDeltaClubController;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketClaimReqVO;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketRespVO;
import cn.iocoder.yudao.module.delta.controller.app.notification.AppDeltaNotificationController;
import cn.iocoder.yudao.module.delta.controller.app.orderpool.AppDeltaOrderPoolController;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.AppDeltaServiceOrderController;
import cn.iocoder.yudao.module.delta.controller.app.settlement.AppDeltaWorkerSettlementController;
import cn.iocoder.yudao.module.delta.controller.app.workerorder.AppDeltaWorkerOrderController;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.service.market.DeltaOrderMarketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppDeltaOrderMarketControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppDeltaOrderMarketController controller;
    @Mock
    private DeltaOrderMarketService marketService;

    private static final Long MEMBER_ID = 10L;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(MEMBER_ID);
        loginUser.setUserType(UserTypeEnum.MEMBER.getValue());
        SecurityFrameworkUtils.setLoginUser(loginUser, mock(HttpServletRequest.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appControllerMappingsDoNotContainAppApiPrefix() {
        assertMapping(AppDeltaOrderPoolController.class, "/delta/order-pool");
        assertMapping(AppDeltaWorkerOrderController.class, "/delta/worker-order");
        assertMapping(AppDeltaServiceOrderController.class, "/delta/service-order");
        assertMapping(AppDeltaWorkerSettlementController.class, "/delta/worker-settlement");
        assertMapping(AppDeltaNotificationController.class, "/delta/notification");
        assertMapping(AppDeltaClubController.class, "/delta/club");
        assertMapping(AppDeltaOrderMarketController.class, "/delta/order-market");
    }

    @Test
    void availablePageUsesLoginMemberId() {
        AppDeltaOrderMarketPageReqVO reqVO = new AppDeltaOrderMarketPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        DeltaOrderMarketListingDO listing = DeltaOrderMarketListingDO.builder()
                .id(1L).listingNo("DML001").serviceType(1)
                .serviceAmount(5000).listingStatus(0).build();
        when(marketService.getAvailablePageForMember(MEMBER_ID, 1, 10))
                .thenReturn(new PageResult<>(Collections.singletonList(listing), 1L));

        PageResult<AppDeltaOrderMarketRespVO> result =
                controller.getAvailablePage(reqVO).getData();
        assertEquals(1, result.getList().size());
        assertEquals("陪玩", result.getList().get(0).getServiceTypeName());
        verify(marketService).getAvailablePageForMember(MEMBER_ID, 1, 10);
    }

    @Test
    void claimUsesOnlyLoginMemberAndListingId() {
        AppDeltaOrderMarketClaimReqVO reqVO = new AppDeltaOrderMarketClaimReqVO();
        reqVO.setId(99L);
        assertTrue(controller.claim(reqVO).getData());
        verify(marketService).claimForMember(MEMBER_ID, 99L);
    }

    @Test
    void appMarketVoDoesNotExposeSensitiveFields() {
        Set<String> fieldNames = Arrays.stream(AppDeltaOrderMarketRespVO.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName).collect(Collectors.toSet());
        for (String forbidden : Arrays.asList("serviceOrderId", "sourceTenantId", "claimedClubId",
                "claimedClubTenantId", "publisherId", "withdrawReason", "remark", "version")) {
            assertFalse(fieldNames.contains(forbidden), forbidden);
        }
    }

    private void assertMapping(Class<?> type, String expected) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{expected}, mapping.value());
        assertFalse(expected.contains("/app-api"));
    }
}
