package cn.iocoder.yudao.module.delta.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationApproveReqVO;
import cn.iocoder.yudao.module.delta.controller.admin.clubapplication.vo.DeltaClubApplicationPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.clubapplication.vo.AppDeltaClubApplicationSubmitReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubApplicationDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubServiceScopeDO;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubApplicationMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubServiceScopeMapper;
import cn.iocoder.yudao.module.delta.dal.redis.lock.DeltaClubApplicationLockRedisDAO;
import cn.iocoder.yudao.module.delta.dal.redis.no.DeltaNoRedisDAO;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubApplicationStatusEnum;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceTypeEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPayload;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * 俱乐部入驻申请 Service 实现
 *
 * @author Delta-Vanguard
 */
@Service
@Validated
@Slf4j
public class DeltaClubApplicationServiceImpl implements DeltaClubApplicationService {

    @Resource
    private DeltaClubApplicationMapper deltaClubApplicationMapper;

    @Resource
    private DeltaClubProfileMapper deltaClubProfileMapper;

    @Resource
    private DeltaClubServiceScopeMapper deltaClubServiceScopeMapper;

    @Resource
    private DeltaNoRedisDAO deltaNoRedisDAO;

    @Resource
    private TenantService tenantService;

    @Resource
    private DeltaEventPublisher deltaEventPublisher;

    @Resource
    private DeltaClubApplicationLockRedisDAO deltaClubApplicationLockRedisDAO;

    // ===== App 端 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApplication(Long memberId, AppDeltaClubApplicationSubmitReqVO reqVO) {
        // 分布式锁保护：同一 memberId 并发提交最终只有一个成功
        return deltaClubApplicationLockRedisDAO.lockAndRun(memberId, () -> {
            // 1. 锁内重新检查是否已有待审核申请（防止并发窗口）
            DeltaClubApplicationDO pendingApp = deltaClubApplicationMapper.selectLatestByMemberId(memberId);
            if (pendingApp != null && DeltaClubApplicationStatusEnum.isPending(pendingApp.getApplicationStatus())) {
                throw exception(CLUB_APPLICATION_PENDING_EXISTS);
            }

            // 2. 检查是否已是其他俱乐部所有者
            DeltaClubProfileDO existProfile = deltaClubProfileMapper.selectByOwnerMemberId(memberId);
            if (existProfile != null) {
                throw exception(CLUB_OWNER_DUPLICATE);
            }

            // 3. 创建申请
            DeltaClubApplicationDO application = DeltaClubApplicationDO.builder()
                    .applicationNo(deltaNoRedisDAO.generateClubApplicationNo())
                    .applicantMemberId(memberId)
                    .clubName(reqVO.getClubName().trim())
                    .contactName(reqVO.getContactName().trim())
                    .contactMobile(reqVO.getContactMobile().trim())
                    .contactWechat(reqVO.getContactWechat())
                    .description(reqVO.getDescription())
                    .logoUrl(reqVO.getLogoUrl())
                    .qualificationUrls(reqVO.getQualificationUrls())
                    .applicationStatus(DeltaClubApplicationStatusEnum.PENDING.getStatus())
                    .version(0)
                    .build();
            try {
                deltaClubApplicationMapper.insert(application);
            } catch (DuplicateKeyException e) {
                log.warn("[submitApplication] 唯一键冲突，可能并发重复提交 member({})", memberId);
                throw exception(CLUB_APPLICATION_PENDING_EXISTS);
            }

            log.info("[submitApplication] 会员 member({}) 提交俱乐部入驻申请 application({})", memberId, application.getId());

            // 4. 发布事件（非租户隔离事件，tenantId 使用 0）
            try {
                deltaEventPublisher.publishToAdmin(DeltaEventPublishReq.builder()
                        .eventType(DeltaEventTypeEnum.CLUB_APPLICATION_SUBMITTED.getType())
                        .tenantId(0L)
                        .aggregateType("CLUB_APPLICATION")
                        .aggregateId(application.getId())
                        .bizKey("CLUB_APP_SUBMIT:" + application.getId())
                        .build());
            } catch (Exception e) {
                log.warn("[submitApplication] 发布事件失败，不阻塞主流程", e);
            }

            return application.getId();
        });
    }

    @Override
    public DeltaClubApplicationDO getMyLatestApplication(Long memberId) {
        return deltaClubApplicationMapper.selectLatestByMemberId(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long memberId) {
        DeltaClubApplicationDO application = deltaClubApplicationMapper.selectLatestByMemberId(memberId);
        if (application == null) {
            throw exception(CLUB_APPLICATION_NOT_EXISTS);
        }
        if (!DeltaClubApplicationStatusEnum.isPending(application.getApplicationStatus())) {
            throw exception(CLUB_APPLICATION_STATUS_NOT_ALLOWED);
        }
        // CAS 更新：WHERE id=? AND application_status=PENDING AND version=?
        int rows = deltaClubApplicationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubApplicationDO>()
                        .set("application_status", DeltaClubApplicationStatusEnum.CANCELED.getStatus())
                        .setSql("version = version + 1")
                        .eq("id", application.getId())
                        .eq("application_status", DeltaClubApplicationStatusEnum.PENDING.getStatus())
                        .eq("version", application.getVersion()));
        if (rows == 0) {
            throw exception(CLUB_APPLICATION_STATUS_NOT_ALLOWED);
        }
        log.info("[cancelApplication] 会员 member({}) 撤销入驻申请 application({})", memberId, application.getId());

        // 发布撤销事件
        try {
            deltaEventPublisher.publishToAdmin(DeltaEventPublishReq.builder()
                    .eventType(DeltaEventTypeEnum.CLUB_APPLICATION_CANCELED.getType())
                    .tenantId(0L)
                    .aggregateType("CLUB_APPLICATION")
                    .aggregateId(application.getId())
                    .recipientId(application.getApplicantMemberId())
                    .bizKey("CLUB_APP_CANCEL:" + application.getId())
                    .payload(DeltaEventPayload.builder()
                            .eventType(DeltaEventTypeEnum.CLUB_APPLICATION_CANCELED.getType())
                            .tenantId(0L)
                            .aggregateId(application.getId())
                            .beforeStatus(DeltaClubApplicationStatusEnum.PENDING.getStatus())
                            .afterStatus(DeltaClubApplicationStatusEnum.CANCELED.getStatus())
                            .titleArgs(Map.of(
                                    "applicationNo", application.getApplicationNo(),
                                    "clubName", application.getClubName(),
                                    "applicationStatus", "CANCELED"))
                            .occurredAt(LocalDateTime.now())
                            .build())
                    .build());
        } catch (Exception e) {
            log.warn("[cancelApplication] 发布撤销事件失败，不阻塞主流程", e);
        }
    }

    // ===== Admin 端 =====

    @Override
    public DeltaClubApplicationDO getApplication(Long id) {
        return deltaClubApplicationMapper.selectById(id);
    }

    @Override
    public PageResult<DeltaClubApplicationDO> getApplicationPage(DeltaClubApplicationPageReqVO pageReqVO) {
        return deltaClubApplicationMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(DeltaClubApplicationApproveReqVO reqVO, Long auditorId) {
        // 1. 校验申请存在且为待审核
        DeltaClubApplicationDO application = validateApplicationPending(reqVO.getId());

        // 2. 校验租户存在
        tenantService.validTenant(reqVO.getTenantId());

        // 3. 检查该租户是否已有俱乐部档案
        DeltaClubProfileDO existProfile = deltaClubProfileMapper.selectByTenantId(reqVO.getTenantId());
        if (existProfile != null) {
            throw exception(CLUB_TENANT_DUPLICATE);
        }

        // 4. 校验服务类型合法
        validateServiceTypes(reqVO.getServiceTypes());

        // 5. 抽成比例范围校验（万分制 0-10000）
        if (reqVO.getPlatformCommissionRate() != null &&
                (reqVO.getPlatformCommissionRate() < 0 || reqVO.getPlatformCommissionRate() > 10000)) {
            throw exception(CLUB_COMMISSION_RATE_INVALID);
        }

        // 6. 在目标租户上下文中创建俱乐部档案和服务范围
        // 用 TenantUtils.execute 确保 tenant_id 自动填充正确，且不会受当前管理员租户影响
        final String clubCode = deltaNoRedisDAO.generateClubCode();
        final DeltaClubApplicationDO appRef = application;
        TenantUtils.execute(reqVO.getTenantId(), () -> {
            // 6a. 创建俱乐部档案
            DeltaClubProfileDO profile = new DeltaClubProfileDO();
            profile.setClubCode(clubCode);
            profile.setClubName(appRef.getClubName());
            profile.setOwnerMemberId(appRef.getApplicantMemberId());
            profile.setContactName(appRef.getContactName());
            profile.setContactMobile(appRef.getContactMobile());
            profile.setContactWechat(appRef.getContactWechat());
            profile.setLogoUrl(appRef.getLogoUrl());
            profile.setDescription(appRef.getDescription());
            profile.setBusinessStatus(DeltaClubBusinessStatusEnum.ENABLED.getStatus());
            profile.setPlatformCommissionRate(reqVO.getPlatformCommissionRate());
            profile.setMaxConcurrentOrders(reqVO.getMaxConcurrentOrders());
            profile.setApplicationId(appRef.getId());
            profile.setRemark(reqVO.getRemark());
            profile.setVersion(0);
            deltaClubProfileMapper.insert(profile);
            log.info("[approveApplication] 创建俱乐部 profile({}) for tenant({})", profile.getId(), reqVO.getTenantId());

            // 6b. 创建服务范围
            for (Integer serviceType : reqVO.getServiceTypes()) {
                DeltaClubServiceScopeDO scope = new DeltaClubServiceScopeDO();
                scope.setClubProfileId(profile.getId());
                scope.setServiceType(serviceType);
                scope.setEnabled(true);
                deltaClubServiceScopeMapper.insert(scope);
            }

            // 7. 发布事件（在目标租户上下文内）
            try {
                deltaEventPublisher.publishToSystem(DeltaEventPublishReq.builder()
                        .eventType(DeltaEventTypeEnum.CLUB_APPLICATION_APPROVED.getType())
                        .tenantId(reqVO.getTenantId())
                        .aggregateType("CLUB_APPLICATION")
                        .aggregateId(appRef.getId())
                        .bizKey("CLUB_APP_APPROVE:" + appRef.getId())
                        .build());
                deltaEventPublisher.publishToSystem(DeltaEventPublishReq.builder()
                        .eventType(DeltaEventTypeEnum.CLUB_CREATED.getType())
                        .tenantId(reqVO.getTenantId())
                        .aggregateType("CLUB_PROFILE")
                        .aggregateId(profile.getId())
                        .bizKey("CLUB_CREATE:" + profile.getId())
                        .build());
            } catch (Exception e) {
                log.warn("[approveApplication] 发布事件失败，不阻塞主流程", e);
            }
        });

        // 8. 更新申请状态（CAS：WHERE id=? AND application_status=PENDING AND version=?）
        int rows = deltaClubApplicationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubApplicationDO>()
                        .set("application_status", DeltaClubApplicationStatusEnum.APPROVED.getStatus())
                        .set("approved_tenant_id", reqVO.getTenantId())
                        .set("auditor_id", auditorId)
                        .set("audit_time", LocalDateTime.now())
                        .setSql("version = version + 1")
                        .eq("id", appRef.getId())
                        .eq("application_status", DeltaClubApplicationStatusEnum.PENDING.getStatus())
                        .eq("version", appRef.getVersion()));
        if (rows == 0) {
            throw exception(CLUB_APPLICATION_STATUS_NOT_ALLOWED);
        }
        log.info("[approveApplication] 审核通过 application({}) tenant({})", appRef.getId(), reqVO.getTenantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long applicationId, String reason, Long auditorId) {
        DeltaClubApplicationDO application = validateApplicationPending(applicationId);
        // CAS 更新：WHERE id=? AND application_status=PENDING AND version=?
        int rows = deltaClubApplicationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<DeltaClubApplicationDO>()
                        .set("application_status", DeltaClubApplicationStatusEnum.REJECTED.getStatus())
                        .set("reject_reason", reason)
                        .set("auditor_id", auditorId)
                        .set("audit_time", LocalDateTime.now())
                        .setSql("version = version + 1")
                        .eq("id", applicationId)
                        .eq("application_status", DeltaClubApplicationStatusEnum.PENDING.getStatus())
                        .eq("version", application.getVersion()));
        if (rows == 0) {
            throw exception(CLUB_APPLICATION_STATUS_NOT_ALLOWED);
        }
        log.info("[rejectApplication] 审核拒绝 application({}), auditor({})", applicationId, auditorId);

        // 发布事件
        try {
            deltaEventPublisher.publishToAdmin(DeltaEventPublishReq.builder()
                    .eventType(DeltaEventTypeEnum.CLUB_APPLICATION_REJECTED.getType())
                    .tenantId(0L)
                    .aggregateType("CLUB_APPLICATION")
                    .aggregateId(application.getId())
                    .recipientId(application.getApplicantMemberId())
                    .bizKey("CLUB_APP_REJECT:" + application.getId())
                    .build());
        } catch (Exception e) {
            log.warn("[rejectApplication] 发布事件失败", e);
        }
    }

    // ===== 内部校验 =====

    private DeltaClubApplicationDO validateApplicationPending(Long applicationId) {
        DeltaClubApplicationDO application = deltaClubApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw exception(CLUB_APPLICATION_NOT_EXISTS);
        }
        if (!DeltaClubApplicationStatusEnum.isPending(application.getApplicationStatus())) {
            throw exception(CLUB_APPLICATION_STATUS_NOT_ALLOWED);
        }
        return application;
    }

    private void validateServiceTypes(List<Integer> serviceTypes) {
        for (Integer type : serviceTypes) {
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
    }

}
