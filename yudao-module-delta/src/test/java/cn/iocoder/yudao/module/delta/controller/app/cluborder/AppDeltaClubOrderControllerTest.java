package cn.iocoder.yudao.module.delta.controller.app.cluborder;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAssignWorkerReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderAvailableWorkerPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderDetailRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderPageRespVO;
import cn.iocoder.yudao.module.delta.controller.app.cluborder.vo.AppDeltaClubOrderWorkerRespVO;
import cn.iocoder.yudao.module.delta.service.cluborder.DeltaClubOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppDeltaClubOrderControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppDeltaClubOrderController controller;
    @Mock
    private DeltaClubOrderService clubOrderService;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(UserTypeEnum.MEMBER.getValue());
        SecurityFrameworkUtils.setLoginUser(loginUser, mock(HttpServletRequest.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void controllerUsesStandardAppPath() {
        RequestMapping mapping = AppDeltaClubOrderController.class.getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/delta/club-order"}, mapping.value());
    }

    @Test
    void assignUsesLoginMemberAndSafeBodyOnly() {
        AppDeltaClubOrderAssignWorkerReqVO reqVO = new AppDeltaClubOrderAssignWorkerReqVO();
        reqVO.setListingId(1L);
        reqVO.setWorkerId(2L);
        assertTrue(controller.assignWorker(reqVO).getData());
        verify(clubOrderService).assignWorkerForMember(10L, reqVO);
        assertEquals(new HashSet<>(Arrays.asList("listingId", "workerId", "remark")),
                fieldNames(AppDeltaClubOrderAssignWorkerReqVO.class));
    }

    @Test
    void requestVosDoNotAcceptIdentityFields() {
        Set<String> fields = new HashSet<>();
        fields.addAll(fieldNames(AppDeltaClubOrderPageReqVO.class));
        fields.addAll(fieldNames(AppDeltaClubOrderAvailableWorkerPageReqVO.class));
        fields.addAll(fieldNames(AppDeltaClubOrderAssignWorkerReqVO.class));
        for (String forbidden : Arrays.asList("clubId", "clubTenantId", "sourceTenantId",
                "memberUserId", "tenantId", "operatorId")) {
            assertFalse(fields.contains(forbidden), forbidden);
        }
    }

    @Test
    void responseVosDoNotExposeSensitiveFields() {
        Set<String> fields = new HashSet<>();
        fields.addAll(fieldNames(AppDeltaClubOrderPageRespVO.class));
        fields.addAll(fieldNames(AppDeltaClubOrderDetailRespVO.class));
        fields.addAll(fieldNames(AppDeltaClubOrderWorkerRespVO.class));
        for (String forbidden : Arrays.asList("buyerUserId", "memberMobile", "contactMobile",
                "tradeOrderId", "tradeOrderNo", "tradeOrderItemId", "sourceTenantId",
                "claimedClubTenantId", "tenantId", "adminRemark", "platformFee",
                "workerAmount", "userId", "realName", "phone", "commissionRate",
                "auditRemark")) {
            assertFalse(fields.contains(forbidden), forbidden);
        }
    }

    private Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
    }
}
