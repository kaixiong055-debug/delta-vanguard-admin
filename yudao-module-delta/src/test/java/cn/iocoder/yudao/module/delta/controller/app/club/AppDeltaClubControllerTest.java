package cn.iocoder.yudao.module.delta.controller.app.club;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.club.vo.AppDeltaClubIdentityRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubApplicationService;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppDeltaClubControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppDeltaClubController controller;
    @Mock
    private DeltaClubApplicationService applicationService;
    @Mock
    private DeltaClubService clubService;

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
    void getIdentity_noApplicationAndNoClub() {
        AppDeltaClubIdentityRespVO result = controller.getIdentity().getData();
        assertFalse(result.getHasApplication());
        assertNull(result.getApplicationStatus());
        assertEquals("未申请", result.getApplicationStatusName());
        assertFalse(result.getIsClubOwner());
        assertFalse(result.getCanUseOrderMarket());
        assertTrue(result.getServiceScopes().isEmpty());
    }

    @Test
    void getIdentity_pendingApplication() {
        when(applicationService.getMyLatestApplication(MEMBER_ID)).thenReturn(
                DeltaClubApplicationDO.builder().applicationStatus(0).build());
        AppDeltaClubIdentityRespVO result = controller.getIdentity().getData();
        assertTrue(result.getHasApplication());
        assertEquals(0, result.getApplicationStatus());
        assertEquals("待审核", result.getApplicationStatusName());
        assertFalse(result.getIsClubOwner());
    }

    @Test
    void getIdentity_rejectedApplication() {
        when(applicationService.getMyLatestApplication(MEMBER_ID)).thenReturn(
                DeltaClubApplicationDO.builder()
                        .applicationStatus(2).rejectReason("资料不完整").build());
        AppDeltaClubIdentityRespVO result = controller.getIdentity().getData();
        assertEquals("已拒绝", result.getApplicationStatusName());
        assertEquals("资料不完整", result.getRejectReason());
        assertFalse(result.getCanUseOrderMarket());
    }

    @Test
    void getIdentity_enabledClub() {
        when(applicationService.getMyLatestApplication(MEMBER_ID)).thenReturn(
                DeltaClubApplicationDO.builder().applicationStatus(1).build());
        DeltaClubProfileDO profile = DeltaClubProfileDO.builder()
                .id(20L).clubCode("DC001").clubName("测试俱乐部")
                .ownerMemberId(MEMBER_ID).businessStatus(1)
                .platformCommissionRate(500).maxConcurrentOrders(10).build();
        when(clubService.getClubProfileByOwnerMemberId(MEMBER_ID)).thenReturn(profile);
        when(clubService.getClubServiceScopes(20L)).thenReturn(Collections.singletonList(
                DeltaClubServiceScopeDO.builder()
                        .serviceType(1).enabled(true).build()));

        AppDeltaClubIdentityRespVO result = controller.getIdentity().getData();
        assertEquals("已通过", result.getApplicationStatusName());
        assertTrue(result.getIsClubOwner());
        assertTrue(result.getCanUseOrderMarket());
        assertEquals("启用", result.getBusinessStatusName());
        assertEquals("陪玩", result.getServiceScopes().get(0).getServiceTypeName());
    }

    @Test
    void getIdentity_disabledClub() {
        when(applicationService.getMyLatestApplication(MEMBER_ID)).thenReturn(
                DeltaClubApplicationDO.builder().applicationStatus(1).build());
        when(clubService.getClubProfileByOwnerMemberId(MEMBER_ID)).thenReturn(
                DeltaClubProfileDO.builder().id(20L).businessStatus(0).build());
        AppDeltaClubIdentityRespVO result = controller.getIdentity().getData();
        assertEquals("已通过", result.getApplicationStatusName());
        assertTrue(result.getIsClubOwner());
        assertFalse(result.getCanUseOrderMarket());
        assertEquals("停用", result.getBusinessStatusName());
    }

    @Test
    void responseDoesNotExposeTenantOrContactFields() {
        Set<String> fieldNames = Arrays.stream(AppDeltaClubIdentityRespVO.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName).collect(Collectors.toSet());
        assertFalse(fieldNames.contains("tenantId"));
        assertFalse(fieldNames.contains("ownerMemberId"));
        assertFalse(fieldNames.contains("contactMobile"));
        assertFalse(fieldNames.contains("contactWechat"));
        assertFalse(fieldNames.contains("remark"));
        assertFalse(fieldNames.contains("applicationId"));
        assertFalse(fieldNames.contains("version"));
    }
}
