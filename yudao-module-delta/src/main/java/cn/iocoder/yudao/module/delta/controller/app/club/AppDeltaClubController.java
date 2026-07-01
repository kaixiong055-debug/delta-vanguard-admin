package cn.iocoder.yudao.module.delta.controller.app.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.delta.controller.app.club.vo.AppDeltaClubIdentityRespVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubApplicationStatusEnum;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubApplicationService;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 俱乐部身份")
@RestController
@RequestMapping("/delta/club")
public class AppDeltaClubController {

    @Resource
    private DeltaClubApplicationService deltaClubApplicationService;
    @Resource
    private DeltaClubService deltaClubService;

    @GetMapping("/identity")
    @Operation(summary = "查询当前会员的俱乐部身份")
    public CommonResult<AppDeltaClubIdentityRespVO> getIdentity() {
        Long memberUserId = getLoginUserId();
        DeltaClubApplicationDO application =
                deltaClubApplicationService.getMyLatestApplication(memberUserId);
        DeltaClubProfileDO profile =
                deltaClubService.getClubProfileByOwnerMemberId(memberUserId);

        AppDeltaClubIdentityRespVO result = buildIdentity(application, profile);
        if (profile != null) {
            result.setServiceScopes(convertScopes(
                    deltaClubService.getClubServiceScopes(profile.getId())));
        }
        return success(result);
    }

    private AppDeltaClubIdentityRespVO buildIdentity(DeltaClubApplicationDO application,
                                                      DeltaClubProfileDO profile) {
        AppDeltaClubIdentityRespVO result = new AppDeltaClubIdentityRespVO();
        result.setHasApplication(application != null);
        result.setApplicationStatus(application != null ? application.getApplicationStatus() : null);
        result.setApplicationStatusName(application != null
                ? getApplicationStatusName(application.getApplicationStatus()) : "未申请");
        result.setRejectReason(application != null ? application.getRejectReason() : null);
        result.setIsClubOwner(profile != null);
        result.setCanUseOrderMarket(profile != null
                && DeltaClubBusinessStatusEnum.isEnabled(profile.getBusinessStatus()));
        if (profile == null) {
            return result;
        }
        result.setClubId(profile.getId());
        result.setClubCode(profile.getClubCode());
        result.setClubName(profile.getClubName());
        result.setLogoUrl(profile.getLogoUrl());
        result.setDescription(profile.getDescription());
        result.setBusinessStatus(profile.getBusinessStatus());
        result.setBusinessStatusName(DeltaClubBusinessStatusEnum.isEnabled(profile.getBusinessStatus())
                ? "启用" : "停用");
        result.setPlatformCommissionRate(profile.getPlatformCommissionRate());
        result.setMaxConcurrentOrders(profile.getMaxConcurrentOrders());
        return result;
    }

    private List<AppDeltaClubIdentityRespVO.ServiceScopeItem> convertScopes(
            List<DeltaClubServiceScopeDO> scopes) {
        return scopes.stream().map(scope -> {
            AppDeltaClubIdentityRespVO.ServiceScopeItem item =
                    new AppDeltaClubIdentityRespVO.ServiceScopeItem();
            item.setServiceType(scope.getServiceType());
            ServiceTypeEnum serviceType = ServiceTypeEnum.valueOf(scope.getServiceType());
            item.setServiceTypeName(serviceType != null ? serviceType.getName() : "未知");
            item.setEnabled(scope.getEnabled());
            return item;
        }).collect(Collectors.toList());
    }

    private String getApplicationStatusName(Integer status) {
        for (DeltaClubApplicationStatusEnum statusEnum : DeltaClubApplicationStatusEnum.values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum.getName();
            }
        }
        return "未知";
    }
}
