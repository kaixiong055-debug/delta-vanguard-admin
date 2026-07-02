package cn.iocoder.yudao.module.delta.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.delta.controller.admin.serviceorder.vo.DeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.serviceorder.vo.AppDeltaServiceOrderPageReqVO;
import cn.iocoder.yudao.module.delta.dal.dataobject.order.DeltaServiceOrderDO;
import cn.iocoder.yudao.module.delta.enums.order.ServiceOrderStatusEnum;
import cn.iocoder.yudao.module.delta.enums.market.DeltaOrderMarketStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentStatusEnum;
import cn.iocoder.yudao.module.delta.enums.order.AssignmentTypeEnum;
import cn.iocoder.yudao.module.delta.enums.order.DispatchModeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务履约订单 Mapper
 *
 * @author Delta-Vanguard
 */
@Mapper
public interface DeltaServiceOrderMapper extends BaseMapperX<DeltaServiceOrderDO> {

    @Select({"<script>",
            "SELECT so.* FROM delta_service_order so",
            "WHERE so.deleted = 0 AND so.assigned_worker_id = #{workerId}",
            "<if test='status != null'> AND so.status = #{status} </if>",
            "AND (so.tenant_id = #{workerTenantId}",
            "<if test='clubId != null'>",
            " OR (so.dispatch_mode = #{clubDispatchMode} AND EXISTS (",
            "   SELECT 1 FROM delta_order_market_listing listing",
            "   WHERE listing.deleted = 0 AND listing.listing_status = #{claimedStatus}",
            "     AND listing.service_order_id = so.id",
            "     AND listing.source_tenant_id = so.tenant_id",
            "     AND listing.claimed_club_id = #{clubId}",
            "     AND listing.claimed_club_tenant_id = #{workerTenantId}",
            " ) AND EXISTS (",
            "   SELECT 1 FROM delta_order_assignment assignment",
            "   WHERE assignment.deleted = 0 AND assignment.tenant_id = so.tenant_id",
            "     AND assignment.service_order_id = so.id",
            "     AND assignment.worker_id = #{workerId}",
            "     AND assignment.assignment_type = #{clubAssignmentType}",
            "     AND assignment.assignment_status = #{acceptedAssignmentStatus}",
            " ))",
            "</if>",
            ") ORDER BY so.id DESC",
            "</script>"})
    IPage<DeltaServiceOrderDO> selectWorkerAccessiblePageSql(
            IPage<DeltaServiceOrderDO> page, @Param("workerId") Long workerId,
            @Param("workerTenantId") Long workerTenantId, @Param("clubId") Long clubId,
            @Param("status") Integer status, @Param("claimedStatus") Integer claimedStatus,
            @Param("clubDispatchMode") Integer clubDispatchMode,
            @Param("clubAssignmentType") Integer clubAssignmentType,
            @Param("acceptedAssignmentStatus") Integer acceptedAssignmentStatus);

    default PageResult<DeltaServiceOrderDO> selectWorkerAccessiblePage(
            cn.iocoder.yudao.framework.common.pojo.PageParam pageParam, Long workerId,
            Long workerTenantId, Long clubId, Integer status) {
        IPage<DeltaServiceOrderDO> page = MyBatisUtils.buildPage(pageParam);
        selectWorkerAccessiblePageSql(page, workerId, workerTenantId, clubId, status,
                DeltaOrderMarketStatusEnum.CLAIMED.getStatus(),
                DispatchModeEnum.CLUB_ASSIGN.getMode(), AssignmentTypeEnum.CLUB_ASSIGN.getType(),
                AssignmentStatusEnum.ACCEPTED.getStatus());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    default DeltaServiceOrderDO selectByTradeOrderItemId(Long tradeOrderItemId) {
        return selectOne(DeltaServiceOrderDO::getTradeOrderItemId, tradeOrderItemId);
    }

    default DeltaServiceOrderDO selectByServiceOrderNo(String serviceOrderNo) {
        return selectOne(DeltaServiceOrderDO::getServiceOrderNo, serviceOrderNo);
    }

    default List<DeltaServiceOrderDO> selectListByTradeOrderItemIds(Collection<Long> tradeOrderItemIds) {
        return selectList(DeltaServiceOrderDO::getTradeOrderItemId, tradeOrderItemIds);
    }

    default PageResult<DeltaServiceOrderDO> selectPage(AppDeltaServiceOrderPageReqVO reqVO) {
        return selectPage(reqVO, buildAppQueryWrapper(reqVO));
    }

    default PageResult<DeltaServiceOrderDO> selectPage(DeltaServiceOrderPageReqVO reqVO) {
        return selectPage(reqVO, buildAdminQueryWrapper(reqVO));
    }

    default LambdaQueryWrapper<DeltaServiceOrderDO> buildAppQueryWrapper(AppDeltaServiceOrderPageReqVO reqVO) {
        return new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eqIfPresent(DeltaServiceOrderDO::getBuyerUserId, reqVO.getUserId())
                .eqIfPresent(DeltaServiceOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, reqVO.getServiceType())
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, reqVO.getDeviceType())
                .betweenIfPresent(DeltaServiceOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaServiceOrderDO::getId);
    }

    default LambdaQueryWrapper<DeltaServiceOrderDO> buildAdminQueryWrapper(DeltaServiceOrderPageReqVO reqVO) {
        return new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eqIfPresent(DeltaServiceOrderDO::getServiceOrderNo, reqVO.getServiceOrderNo())
                .eqIfPresent(DeltaServiceOrderDO::getTradeOrderNo, reqVO.getTradeOrderNo())
                .eqIfPresent(DeltaServiceOrderDO::getBuyerUserId, reqVO.getBuyerUserId())
                .eqIfPresent(DeltaServiceOrderDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(DeltaServiceOrderDO::getSkuId, reqVO.getSkuId())
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, reqVO.getServiceType())
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(DeltaServiceOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DeltaServiceOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeltaServiceOrderDO::getId);
    }

    /**
     * CAS 条件更新服务单状态
     *
     * @param id       服务单ID
     * @param newStatus 新状态
     * @param oldStatus 旧状态（CAS条件）
     * @param updates  其他需要更新的字段
     * @return 受影响行数
     */
    default int updateStatusCas(Long id, Integer newStatus, Integer oldStatus,
                                 java.util.function.Consumer<LambdaUpdateWrapper<DeltaServiceOrderDO>> updates) {
        LambdaUpdateWrapper<DeltaServiceOrderDO> wrapper = new LambdaUpdateWrapper<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getId, id)
                .eq(DeltaServiceOrderDO::getStatus, oldStatus)
                .set(DeltaServiceOrderDO::getStatus, newStatus);
        if (updates != null) {
            updates.accept(wrapper);
        }
        return update(null, wrapper);
    }

    /**
     * 订单池分页查询（只包含POOL_PENDING状态+未分配打手的订单+技能匹配）
     */
    default PageResult<DeltaServiceOrderDO> selectPoolPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
                                                            Integer poolStatus, Integer deviceType, Integer serviceType) {
        LambdaQueryWrapperX<DeltaServiceOrderDO> wrapper = new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getStatus, poolStatus)
                .eqIfPresent(DeltaServiceOrderDO::getDeviceType, deviceType)
                .eqIfPresent(DeltaServiceOrderDO::getServiceType, serviceType)
                .orderByDesc(DeltaServiceOrderDO::getId);
        // isNull 放在最后调用，因为父类 isNull 返回 LambdaQueryWrapper，不支持后续 eqIfPresent 链式调用
        wrapper.isNull(DeltaServiceOrderDO::getAssignedWorkerId);
        return selectPage(pageParam, wrapper);
    }

    /**
     * 打手已分配订单分页查询
     */
    default PageResult<DeltaServiceOrderDO> selectWorkerPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
                                                              Long assignedWorkerId, Integer status) {
        LambdaQueryWrapperX<DeltaServiceOrderDO> wrapper = new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getAssignedWorkerId, assignedWorkerId)
                .eqIfPresent(DeltaServiceOrderDO::getStatus, status)
                .orderByDesc(DeltaServiceOrderDO::getId);
        return selectPage(pageParam, wrapper);
    }

    /** 查询所有租户中已有有效履约订单的打手 ID。调用方必须显式忽略租户过滤。 */
    default Set<Long> selectActiveAssignedWorkerIds() {
        List<DeltaServiceOrderDO> orders = selectList(new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .isNotNull(DeltaServiceOrderDO::getAssignedWorkerId)
                .in(DeltaServiceOrderDO::getStatus,
                        ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus(),
                        ServiceOrderStatusEnum.WAITING_DESIGNATED.getStatus(),
                        ServiceOrderStatusEnum.POOL_PENDING.getStatus(),
                        ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                        ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                        ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                        ServiceOrderStatusEnum.PENDING_VERIFICATION.getStatus())
                .select(DeltaServiceOrderDO::getAssignedWorkerId));
        if (orders.isEmpty()) {
            return new HashSet<>();
        }
        return orders.stream().map(DeltaServiceOrderDO::getAssignedWorkerId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }

    /** 跨租户校验指定打手是否已有有效履约订单。调用方必须显式忽略租户过滤。 */
    default boolean existsActiveByWorkerId(Long workerId) {
        return selectCount(new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getAssignedWorkerId, workerId)
                .in(DeltaServiceOrderDO::getStatus,
                        ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus(),
                        ServiceOrderStatusEnum.WAITING_DESIGNATED.getStatus(),
                        ServiceOrderStatusEnum.POOL_PENDING.getStatus(),
                        ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                        ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                        ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                        ServiceOrderStatusEnum.PENDING_VERIFICATION.getStatus())) > 0;
    }

    /** 跨租户检查打手除指定订单外是否还有有效履约订单。调用方必须显式忽略租户过滤。 */
    default boolean existsOtherActiveByWorkerId(Long workerId, Long excludedServiceOrderId) {
        return selectCount(new LambdaQueryWrapperX<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getAssignedWorkerId, workerId)
                .ne(DeltaServiceOrderDO::getId, excludedServiceOrderId)
                .in(DeltaServiceOrderDO::getStatus,
                        ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus(),
                        ServiceOrderStatusEnum.WAITING_DESIGNATED.getStatus(),
                        ServiceOrderStatusEnum.POOL_PENDING.getStatus(),
                        ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus(),
                        ServiceOrderStatusEnum.IN_PROGRESS.getStatus(),
                        ServiceOrderStatusEnum.WORKER_SUBMITTED.getStatus(),
                        ServiceOrderStatusEnum.PENDING_VERIFICATION.getStatus())) > 0;
    }

    /** 俱乐部分派 CAS：只允许未指派的待派单服务订单。 */
    default int updateClubAssignCas(Long id, Long workerId, Integer dispatchMode,
                                    java.time.LocalDateTime acceptedAt) {
        return update(null, new LambdaUpdateWrapper<DeltaServiceOrderDO>()
                .eq(DeltaServiceOrderDO::getId, id)
                .eq(DeltaServiceOrderDO::getStatus,
                        ServiceOrderStatusEnum.PENDING_DISPATCH.getStatus())
                .isNull(DeltaServiceOrderDO::getAssignedWorkerId)
                .set(DeltaServiceOrderDO::getStatus,
                        ServiceOrderStatusEnum.ACCEPTED_PENDING_START.getStatus())
                .set(DeltaServiceOrderDO::getAssignedWorkerId, workerId)
                .set(DeltaServiceOrderDO::getAcceptedAt, acceptedAt)
                .set(DeltaServiceOrderDO::getDispatchMode, dispatchMode));
    }

}
