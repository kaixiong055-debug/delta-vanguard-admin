package cn.iocoder.yudao.module.delta.service.cluborder;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderAssignmentDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaOrderLogDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketListingMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.club.DeltaClubProfileMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.market.DeltaOrderMarketLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderAssignmentMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaOrderLogMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.order.DeltaServiceOrderMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerMapper;
import cn.iocoder.yudao.module.delta.dal.mysql.worker.DeltaWorkerSkillMapper;
import cn.iocoder.yudao.module.delta.enums.event.DeltaEventTypeEnum;
import cn.iocoder.yudao.module.delta.enums.event.DeltaNotificationTemplateEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketOperationTypeEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.DispatchModeEnum;
import cn.iocoder.yudao.module.delta.enums.order.OperatorTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerAuditStatusEnum;
import cn.iocoder.yudao.module.delta.enums.worker.WorkerWorkStatusEnum;
import cn.iocoder.yudao.module.delta.enums.club.DeltaClubBusinessStatusEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPayload;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublishReq;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

@Service
public class DeltaClubOrderAssignmentCoreService {

    @Resource
    private DeltaOrderMarketListingMapper listingMapper;
    @Resource
    private DeltaClubProfileMapper clubProfileMapper;
    @Resource
    private DeltaOrderMarketLogMapper marketLogMapper;
    @Resource
    private DeltaServiceOrderMapper serviceOrderMapper;
    @Resource
    private DeltaOrderAssignmentMapper assignmentMapper;
    @Resource
    private DeltaOrderLogMapper orderLogMapper;
    @Resource
    private DeltaWorkerMapper workerMapper;
    @Resource
    private DeltaWorkerSkillMapper workerSkillMapper;
    @Resource
    private DeltaEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public void assign(Long listingId, Long clubId, Long clubTenantId,
                       Long memberUserId, Long workerId, String remark) {
        DeltaOrderMarketListingDO listing = TenantUtils.executeIgnore(() -> listingMapper.selectById(listingId));
        validateListing(listing, clubId, clubTenantId);
        validateClub(clubId, clubTenantId, memberUserId);

        DeltaServiceOrderDO order = loadOrder(listing);
        validateOrder(order, listing.getSourceTenantId());
        DeltaOrderAssignmentDO activeAssignment = TenantUtils.execute(listing.getSourceTenantId(),
                () -> assignmentMapper.selectAnyActiveByServiceOrderId(order.getId()));
        if (activeAssignment != null) {
            throw exception(CLUB_ORDER_ALREADY_ASSIGNED);
        }

        DeltaWorkerDO worker = loadWorker(workerId, clubTenantId);
        validateWorker(worker, clubTenantId);
        boolean skillMatched = TenantUtils.execute(clubTenantId,
                () -> workerSkillMapper.hasMatchingSkill(workerId,
                        order.getDeviceType(), order.getServiceType()));
        if (!skillMatched) {
            throw exception(CLUB_ORDER_WORKER_SKILL_NOT_MATCH);
        }
        boolean hasActiveOrder = TenantUtils.executeIgnore(
                () -> serviceOrderMapper.existsActiveByWorkerId(workerId));
        if (hasActiveOrder) {
            throw exception(CLUB_ORDER_WORKER_HAS_ACTIVE_ORDER);
        }

        LocalDateTime now = LocalDateTime.now();
        int orderRows = TenantUtils.execute(listing.getSourceTenantId(), () ->
                serviceOrderMapper.updateClubAssignCas(order.getId(), workerId,
                        DispatchModeEnum.CLUB_ASSIGN.getMode(), now));
        if (orderRows != 1) {
            throw exception(CLUB_ORDER_ASSIGN_STATUS_CHANGED);
        }

        TenantUtils.execute(listing.getSourceTenantId(), () -> {
            assignmentMapper.insert(DeltaOrderAssignmentDO.builder()
                    .serviceOrderId(order.getId())
                    .workerId(workerId)
                    .assignmentType(AssignmentTypeEnum.CLUB_ASSIGN.getType())
                    .assignmentStatus(AssignmentStatusEnum.ACCEPTED.getStatus())
                    .operatorType(OperatorTypeEnum.CLUB_OWNER.getType())
                    .operatorId(memberUserId)
                    .reason(remark)
                    .acceptedAt(now)
                    .build());
            orderLogMapper.insert(DeltaOrderLogDO.builder()
                    .serviceOrderId(order.getId())
                    .operatorType(OperatorTypeEnum.CLUB_OWNER.getType())
                    .operatorId(memberUserId)
                    .operation("俱乐部分派打手")
                    .beforeStatus(ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus())
                    .afterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus())
                    .content(buildOrderLogContent(workerId, remark))
                    .build());
        });

        int workerRows = TenantUtils.execute(clubTenantId, () ->
                workerMapper.updateWorkStatusCas(workerId,
                        WorkerWorkStatusEnum.BUSY.getStatus(),
                        WorkerWorkStatusEnum.ONLINE.getStatus()));
        if (workerRows != 1) {
            throw exception(CLUB_ORDER_ASSIGN_STATUS_CHANGED);
        }

        marketLogMapper.insert(DeltaOrderMarketLogDO.builder()
                .listingId(listing.getId())
                .serviceOrderId(order.getId())
                .operationType(DeltaOrderMarketOperationTypeEnum.ASSIGN_WORKER.getType())
                .operatorType("CLUB")
                .operatorId(memberUserId)
                .clubId(clubId)
                .clubTenantId(clubTenantId)
                .beforeStatus(DeltaOrderMarketStatusEnum.CLAIMED.getStatus())
                .afterStatus(DeltaOrderMarketStatusEnum.CLAIMED.getStatus())
                .success(1)
                .remark(remark)
                .build());

        publishAssignedEvent(listing, order, worker, memberUserId, clubTenantId, now);
    }

    private void validateListing(DeltaOrderMarketListingDO listing, Long clubId, Long clubTenantId) {
        if (listing == null) {
            throw exception(CLUB_ORDER_LISTING_NOT_EXISTS);
        }
        if (!DeltaOrderMarketStatusEnum.CLAIMED.getStatus().equals(listing.getListingStatus())) {
            throw exception(CLUB_ORDER_LISTING_STATUS_INVALID);
        }
        if (!Objects.equals(listing.getClaimedClubId(), clubId)
                || !Objects.equals(listing.getClaimedClubTenantId(), clubTenantId)) {
            throw exception(CLUB_ORDER_NOT_BELONG_TO_CLUB);
        }
        if (listing.getServiceOrderId() == null || listing.getSourceTenantId() == null) {
            throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
        }
    }

    private void validateClub(Long clubId, Long clubTenantId, Long memberUserId) {
        DeltaClubProfileDO club = TenantUtils.execute(clubTenantId,
                () -> clubProfileMapper.selectById(clubId));
        if (club == null || !Objects.equals(club.getTenantId(), clubTenantId)
                || !Objects.equals(club.getOwnerMemberId(), memberUserId)) {
            throw exception(CLUB_ORDER_NOT_BELONG_TO_CLUB);
        }
        if (!DeltaClubBusinessStatusEnum.ENABLED.getStatus().equals(club.getBusinessStatus())) {
            throw exception(ORDER_MARKET_CLUB_DISABLED);
        }
    }

    private DeltaServiceOrderDO loadOrder(DeltaOrderMarketListingDO listing) {
        DeltaServiceOrderDO order = TenantUtils.execute(listing.getSourceTenantId(),
                () -> serviceOrderMapper.selectById(listing.getServiceOrderId()));
        if (order == null) {
            DeltaServiceOrderDO actual = TenantUtils.executeIgnore(
                    () -> serviceOrderMapper.selectById(listing.getServiceOrderId()));
            if (actual != null && !Objects.equals(actual.getTenantId(), listing.getSourceTenantId())) {
                throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
            }
            throw exception(CLUB_ORDER_SERVICE_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private void validateOrder(DeltaServiceOrderDO order, Long sourceTenantId) {
        if (!Objects.equals(order.getTenantId(), sourceTenantId)) {
            throw exception(CLUB_ORDER_SOURCE_TENANT_MISMATCH);
        }
        if (order.getAssignedWorkerId() != null) {
            throw exception(CLUB_ORDER_ALREADY_ASSIGNED);
        }
        if (!ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus().equals(order.getStatus())) {
            throw exception(CLUB_ORDER_STATUS_NOT_ASSIGNABLE);
        }
    }

    private DeltaWorkerDO loadWorker(Long workerId, Long clubTenantId) {
        DeltaWorkerDO worker = TenantUtils.execute(clubTenantId, () -> workerMapper.selectById(workerId));
        if (worker != null) {
            return worker;
        }
        DeltaWorkerDO actual = TenantUtils.executeIgnore(() -> workerMapper.selectById(workerId));
        if (actual != null && !Objects.equals(actual.getTenantId(), clubTenantId)) {
            throw exception(CLUB_ORDER_WORKER_TENANT_MISMATCH);
        }
        throw exception(CLUB_ORDER_WORKER_NOT_EXISTS);
    }

    private void validateWorker(DeltaWorkerDO worker, Long clubTenantId) {
        if (!Objects.equals(worker.getTenantId(), clubTenantId)) {
            throw exception(CLUB_ORDER_WORKER_TENANT_MISMATCH);
        }
        if (!WorkerAuditStatusEnum.APPROVED.getStatus().equals(worker.getAuditStatus())
                || !CommonStatusEnum.ENABLE.getStatus().equals(worker.getStatus())
                || !WorkerWorkStatusEnum.ONLINE.getStatus().equals(worker.getWorkStatus())) {
            throw exception(CLUB_ORDER_WORKER_NOT_AVAILABLE);
        }
    }

    private String buildOrderLogContent(Long workerId, String remark) {
        return remark == null || remark.trim().isEmpty()
                ? "指派打手 ID=" + workerId
                : "指派打手 ID=" + workerId + "，备注：" + remark;
    }

    private void publishAssignedEvent(DeltaOrderMarketListingDO listing,
                                      DeltaServiceOrderDO order, DeltaWorkerDO worker,
                                      Long memberUserId, Long clubTenantId, LocalDateTime now) {
        DeltaEventTypeEnum eventType = DeltaEventTypeEnum.CLUB_ORDER_WORKER_ASSIGNED;
        DeltaEventPayload payload = DeltaEventPayload.builder()
                .eventType(eventType.getType())
                .tenantId(clubTenantId)
                .aggregateId(order.getId())
                .serviceOrderId(order.getId())
                .serviceOrderNo(order.getServiceOrderNo())
                .workerId(worker.getId())
                .workerUserId(worker.getUserId())
                .operatorType(OperatorTypeEnum.CLUB_OWNER.getType())
                .operatorId(memberUserId)
                .beforeStatus(ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus())
                .afterStatus(ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus())
                .occurredAt(now)
                .build();
        TenantUtils.execute(clubTenantId, () -> eventPublisher.publishToWorker(
                DeltaEventPublishReq.builder()
                        .eventType(eventType.getType())
                        .tenantId(clubTenantId)
                        .aggregateType("SERVICE_ORDER")
                        .aggregateId(order.getId())
                        .bizKey(eventType.getType() + ":" + listing.getId() + ":"
                                + order.getId() + ":" + worker.getId())
                        .recipientId(worker.getUserId())
                        .payload(payload)
                        .templateCode(DeltaNotificationTemplateEnum.ORDER_DISPATCHED.getCode())
                        .templateParams(Collections.singletonMap("orderNo", order.getServiceOrderNo()))
                        .build()));
    }
}
