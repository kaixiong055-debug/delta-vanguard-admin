package cn.iocoder.yudao.module.delta.service.market;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;

import java.util.List;

/**
 * 平台订单市场 Service 接口
 *
 * @author Delta-Vanguard
 */
public interface DeltaOrderMarketService {

    // ========== 平台挂牌操作 ==========

    /**
     * 挂牌分页查询（平台管理员）
     */
    PageResult<DeltaOrderMarketListingDO> getListingPage(DeltaOrderMarketListingPageReqVO reqVO);

    /**
     * 挂牌详情
     */
    DeltaOrderMarketListingDO getListing(Long id);

    /**
     * 发布到市场
     * @param publisherId 发布人（当前登录管理员ID）
     */
    DeltaOrderMarketListingDO publish(DeltaOrderMarketPublishReqVO reqVO, Long publisherId);

    /**
     * 撤回挂牌
     * @param operatorId 操作人（当前登录管理员ID）
     */
    void withdraw(DeltaOrderMarketWithdrawReqVO reqVO, Long operatorId);

    /**
     * 平台指定俱乐部接单
     * @param operatorId 操作人（当前登录管理员ID）
     */
    void assign(DeltaOrderMarketAssignReqVO reqVO, Long operatorId);

    // ========== 俱乐部操作 ==========

    /**
     * 俱乐部抢单
     */
    void claim(Long listingId);

    /**
     * 俱乐部可抢订单分页
     */
    PageResult<DeltaOrderMarketListingDO> getAvailablePage(Integer pageNo, Integer pageSize);

    /**
     * 俱乐部可抢订单详情
     */
    DeltaOrderMarketListingDO getAvailable(Long id);

    /**
     * 俱乐部已接订单分页
     */
    PageResult<DeltaOrderMarketListingDO> getMyClaimedPage(Integer pageNo, Integer pageSize);

    // ========== 会员 App 俱乐部操作 ==========

    PageResult<DeltaOrderMarketListingDO> getAvailablePageForMember(
            Long memberUserId, Integer pageNo, Integer pageSize);

    DeltaOrderMarketListingDO getAvailableForMember(Long memberUserId, Long listingId);

    void claimForMember(Long memberUserId, Long listingId);

    PageResult<DeltaOrderMarketListingDO> getMyClaimedPageForMember(
            Long memberUserId, Integer pageNo, Integer pageSize);

    // ========== 日志 ==========

    /**
     * 获取挂牌的操作日志
     */
    List<DeltaOrderMarketLogDO> getListingLogs(Long listingId);
}
