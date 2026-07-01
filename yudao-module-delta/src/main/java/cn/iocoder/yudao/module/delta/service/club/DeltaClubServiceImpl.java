package cn.iocoder.yudao.module.delta.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.controller.admin.club.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 俱乐部 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaClubServiceImpl implements DeltaClubService {

    @Resource
    private DeltaClubProfileMapper deltaClubProfileMapper;

    @Resource
    private DeltaClubServiceScopeMapper deltaClubServiceScopeMapper;

    @Resource
    private DeltaEventPublisher deltaEventPublisher;

    @Override
    public DeltaClubProfileDO getClubProfileByOwnerMemberId(Long memberUserId) {
        return TenantUtils.executeIgnore(() ->
                deltaClubProfileMapper.selectByOwnerMemberId(memberUserId));
    }

    @Override
    public List<DeltaClubServiceScopeDO> getClubServiceScopes(Long clubProfileId) {
        return TenantUtils.executeIgnore(() ->
                deltaClubServiceScopeMapper.selectListByClubProfileId(clubProfileId));
    }

    @Override
    public PageResult<DeltaClubProfileDO> getClubPage(DeltaClubPageReqVO pageReqVO) {
        // 平台管理员查看全部俱乐部时，忽略租户过滤
        return TenantUtils.executeIgnore(() -> deltaClubProfileMapper.selectPage(pageReqVO));
    }

    @Override
    public DeltaClubRespVO getClub(Long id) {
        // 平台管理员查看任意俱乐部时，忽略租户过滤
        DeltaClubProfileDO profile = TenantUtils.executeIgnore(() -> deltaClubProfileMapper.selectById(id));
        if (profile == null) {
            throw exception(CLUB_NOT_EXISTS);
        }
        return buildRespVO(profile);
    }

    @Override
    public DeltaClubRespVO getCurrentClub() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw exception(CLUB_NOT_EXISTS);
        }
        DeltaClubProfileDO profile = deltaClubProfileMapper.selectByTenantId(tenantId);
        if (profile == null) {
            throw exception(CLUB_NOT_EXISTS);
        }
        return buildRespVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClub(DeltaClubUpdateReqVO reqVO) {
        // 跨租户读取：平台管理员可操作任意俱乐部
        DeltaClubProfileDO profile = TenantUtils.executeIgnore(() -> deltaClubProfileMapper.selectById(reqVO.getId()));
        if (profile == null) {
            throw exception(CLUB_NOT_EXISTS);
        }

        // 抽成比例校验
        if (reqVO.getPlatformCommissionRate() != null) {
            if (reqVO.getPlatformCommissionRate() < 0 || reqVO.getPlatformCommissionRate() > 10000) {
                throw exception(CLUB_COMMISSION_RATE_INVALID);
            }
        }

        final Long clubTenantId = profile.getTenantId();
        final Integer oldVersion = profile.getVersion();

        // 进入目标租户上下文执行写入
        TenantUtils.execute(clubTenantId, () -> {
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubProfileDO> uw =
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubProfileDO>()
                            .set("club_name", reqVO.getClubName())
                            .setSql("version = version + 1")
                            .eq("id", reqVO.getId())
                            .eq("version", oldVersion);
            if (reqVO.getContactName() != null) uw.set("contact_name", reqVO.getContactName());
            if (reqVO.getContactMobile() != null) uw.set("contact_mobile", reqVO.getContactMobile());
            if (reqVO.getContactWechat() != null) uw.set("contact_wechat", reqVO.getContactWechat());
            if (reqVO.getLogoUrl() != null) uw.set("logo_url", reqVO.getLogoUrl());
            if (reqVO.getDescription() != null) uw.set("description", reqVO.getDescription());
            if (reqVO.getPlatformCommissionRate() != null) uw.set("platform_commission_rate", reqVO.getPlatformCommissionRate());
            if (reqVO.getMaxConcurrentOrders() != null) uw.set("max_concurrent_orders", reqVO.getMaxConcurrentOrders());
            if (reqVO.getRemark() != null) uw.set("remark", reqVO.getRemark());
            int rows = deltaClubProfileMapper.update(null, uw);
            if (rows == 0) {
                throw exception(CLUB_NOT_EXISTS);
            }
            log.info("[updateClub] 更新俱乐部 profile({}) in tenant({})", reqVO.getId(), clubTenantId);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClubStatus(DeltaClubUpdateStatusReqVO reqVO) {
        // 跨租户读取：平台管理员可操作任意俱乐部
        DeltaClubProfileDO profile = TenantUtils.executeIgnore(() -> deltaClubProfileMapper.selectById(reqVO.getId()));
        if (profile == null) {
            throw exception(CLUB_NOT_EXISTS);
        }

        // 校验状态值
        if (!DeltaClubBusinessStatusEnum.isEnabled(reqVO.getBusinessStatus())
                && !DeltaClubBusinessStatusEnum.isDisabled(reqVO.getBusinessStatus())) {
            throw exception(CLUB_STATUS_NOT_ALLOWED);
        }

        final Long clubTenantId = profile.getTenantId();
        final Integer oldVersion = profile.getVersion();
        final Integer oldBusinessStatus = profile.getBusinessStatus();

        // 进入目标租户上下文执行写入
        TenantUtils.execute(clubTenantId, () -> {
            // CAS 更新：WHERE id=? AND business_status=? AND version=?
            int rows = deltaClubProfileMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubProfileDO>()
                            .set("business_status", reqVO.getBusinessStatus())
                            .setSql("version = version + 1")
                            .eq("id", reqVO.getId())
                            .eq("business_status", oldBusinessStatus)
                            .eq("version", oldVersion));
            if (rows == 0) {
                throw exception(CLUB_STATUS_NOT_ALLOWED);
            }
            log.info("[updateClubStatus] 俱乐部 profile({}) 状态变更为 {} in tenant({})", reqVO.getId(), reqVO.getBusinessStatus(), clubTenantId);

            // 发布事件（在目标租户上下文内）
            try {
                deltaEventPublisher.publishToSystem(DeltaEventPublishReq.builder()
                        .eventType(DeltaEventTypeEnum.CLUB_STATUS_CHANGED.getType())
                        .tenantId(clubTenantId)
                        .aggregateType("CLUB_PROFILE")
                        .aggregateId(profile.getId())
                        .bizKey("CLUB_STATUS:" + profile.getId() + ":" + System.currentTimeMillis())
                        .build());
            } catch (Exception e) {
                log.warn("[updateClubStatus] 发布事件失败", e);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClubServiceScope(DeltaClubUpdateServiceScopeReqVO reqVO) {
        // 跨租户读取：平台管理员可操作任意俱乐部
        DeltaClubProfileDO profile = TenantUtils.executeIgnore(() -> deltaClubProfileMapper.selectById(reqVO.getClubId()));
        if (profile == null) {
            throw exception(CLUB_NOT_EXISTS);
        }

        // 校验服务类型合法
        for (Integer type : reqVO.getServiceTypes()) {
            boolean valid = false;
            for (ServiceTypeEnum st : ServiceTypeEnum.values()) {
                if (Objects.equals(st.getType(), type)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw exception(CLUB_SERVICE_SCOPE_INVALID);
            }
        }

        final Long clubTenantId = profile.getTenantId();

        // 事务内全量替换：先删后建（在目标租户上下文中执行）
        TenantUtils.execute(clubTenantId, () -> {
            deltaClubServiceScopeMapper.deleteByClubProfileId(profile.getId());
            for (Integer serviceType : reqVO.getServiceTypes()) {
                DeltaClubServiceScopeDO scope = new DeltaClubServiceScopeDO();
                scope.setClubProfileId(profile.getId());
                scope.setServiceType(serviceType);
                scope.setEnabled(true);
                deltaClubServiceScopeMapper.insert(scope);
            }
            log.info("[updateClubServiceScope] 俱乐部 profile({}) 服务范围更新为 {} in tenant({})", profile.getId(), reqVO.getServiceTypes(), clubTenantId);

            // 发布事件（在目标租户上下文内）
            try {
                deltaEventPublisher.publishToSystem(DeltaEventPublishReq.builder()
                        .eventType(DeltaEventTypeEnum.CLUB_SERVICE_SCOPE_CHANGED.getType())
                        .tenantId(clubTenantId)
                        .aggregateType("CLUB_PROFILE")
                        .aggregateId(profile.getId())
                        .bizKey("CLUB_SCOPE:" + profile.getId() + ":" + System.currentTimeMillis())
                        .build());
            } catch (Exception e) {
                log.warn("[updateClubServiceScope] 发布事件失败", e);
            }
        });
    }

    // ===== 内部工具方法 =====

    private DeltaClubRespVO buildRespVO(DeltaClubProfileDO profile) {
        DeltaClubRespVO vo = new DeltaClubRespVO();
        vo.setId(profile.getId());
        vo.setTenantId(profile.getTenantId());
        vo.setClubCode(profile.getClubCode());
        vo.setClubName(profile.getClubName());
        vo.setOwnerMemberId(profile.getOwnerMemberId());
        vo.setContactName(profile.getContactName());
        vo.setContactMobile(profile.getContactMobile());
        vo.setContactWechat(profile.getContactWechat());
        vo.setLogoUrl(profile.getLogoUrl());
        vo.setDescription(profile.getDescription());
        vo.setBusinessStatus(profile.getBusinessStatus());
        vo.setBusinessStatusName(getBusinessStatusName(profile.getBusinessStatus()));
        vo.setPlatformCommissionRate(profile.getPlatformCommissionRate());
        vo.setMaxConcurrentOrders(profile.getMaxConcurrentOrders());
        vo.setApplicationId(profile.getApplicationId());
        vo.setRemark(profile.getRemark());
        vo.setCreateTime(profile.getCreateTime());
        vo.setUpdateTime(profile.getUpdateTime());

        // 服务范围
        List<DeltaClubServiceScopeDO> scopes = deltaClubServiceScopeMapper.selectListByClubProfileId(profile.getId());
        List<DeltaClubRespVO.ServiceScopeItem> items = new ArrayList<>();
        for (DeltaClubServiceScopeDO scope : scopes) {
            DeltaClubRespVO.ServiceScopeItem item = new DeltaClubRespVO.ServiceScopeItem();
            item.setId(scope.getId());
            item.setServiceType(scope.getServiceType());
            item.setServiceTypeName(getServiceTypeName(scope.getServiceType()));
            item.setEnabled(scope.getEnabled());
            item.setRemark(scope.getRemark());
            items.add(item);
        }
        vo.setServiceScopes(items);

        return vo;
    }

    private String getBusinessStatusName(Integer status) {
        if (DeltaClubBusinessStatusEnum.isEnabled(status)) return "启用";
        if (DeltaClubBusinessStatusEnum.isDisabled(status)) return "停用";
        return "-";
    }

    private String getServiceTypeName(Integer type) {
        for (ServiceTypeEnum st : ServiceTypeEnum.values()) {
            if (Objects.equals(st.getType(), type)) return st.getName();
        }
        return "-";
    }

}
