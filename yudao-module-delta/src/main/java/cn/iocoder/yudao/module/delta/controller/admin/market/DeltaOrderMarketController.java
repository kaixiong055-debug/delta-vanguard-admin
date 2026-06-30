package cn.iocoder.yudao.module.delta.controller.admin.market;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.*;
import cn.iocoder.yudao.module.delta.convert.market.DeltaOrderMarketConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketLogDO;
import cn.iocoder.yudao.module.delta.service.market.DeltaOrderMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 平台订单市场 Controller（平台管理员接口）
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 平台订单市场")
@RestController
@RequestMapping("/delta/order-market")
@Validated
public class DeltaOrderMarketController {

    @Resource
    private DeltaOrderMarketService deltaOrderMarketService;

    // ========== 挂牌管理 ==========

    @GetMapping("/page")
    @Operation(summary = "挂牌分页查询")
    @PreAuthorize("@ss.hasPermission('delta:order-market:query')")
    public CommonResult<PageResult<DeltaOrderMarketListingRespVO>> page(@Valid DeltaOrderMarketListingPageReqVO reqVO) {
        PageResult<DeltaOrderMarketListingDO> page = deltaOrderMarketService.getListingPage(reqVO);
        PageResult<DeltaOrderMarketListingRespVO> result = DeltaOrderMarketConvert.INSTANCE.convertPage(page);
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(result.getList());
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "挂牌详情")
    @PreAuthorize("@ss.hasPermission('delta:order-market:query')")
    public CommonResult<DeltaOrderMarketListingRespVO> get(@RequestParam("id") Long id) {
        DeltaOrderMarketListingDO listing = deltaOrderMarketService.getListing(id);
        if (listing == null) {
            return success(null);
        }
        DeltaOrderMarketListingRespVO vo = DeltaOrderMarketConvert.INSTANCE.convert(listing);
        // 填充名称
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(Collections.singletonList(vo));
        return success(vo);
    }

    @GetMapping("/logs")
    @Operation(summary = "挂牌操作日志")
    @PreAuthorize("@ss.hasPermission('delta:order-market:query')")
    public CommonResult<List<DeltaOrderMarketLogRespVO>> logs(@RequestParam("listingId") Long listingId) {
        List<DeltaOrderMarketLogDO> logs = deltaOrderMarketService.getListingLogs(listingId);
        List<DeltaOrderMarketLogRespVO> voList = DeltaOrderMarketConvert.INSTANCE.convertLogList(logs);
        DeltaOrderMarketConvert.INSTANCE.fillLogOperationTypeNames(voList);
        return success(voList);
    }

    // ========== 平台操作 ==========

    @PostMapping("/publish")
    @Operation(summary = "发布到市场")
    @PreAuthorize("@ss.hasPermission('delta:order-market:publish')")
    public CommonResult<DeltaOrderMarketListingRespVO> publish(@Valid @RequestBody DeltaOrderMarketPublishReqVO reqVO) {
        Long publisherId = SecurityFrameworkUtils.getLoginUserId();
        DeltaOrderMarketListingDO listing = deltaOrderMarketService.publish(reqVO, publisherId);
        DeltaOrderMarketListingRespVO vo = DeltaOrderMarketConvert.INSTANCE.convert(listing);
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(Collections.singletonList(vo));
        return success(vo);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "撤回挂牌")
    @PreAuthorize("@ss.hasPermission('delta:order-market:withdraw')")
    public CommonResult<Boolean> withdraw(@Valid @RequestBody DeltaOrderMarketWithdrawReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        deltaOrderMarketService.withdraw(reqVO, operatorId);
        return success(true);
    }

    @PostMapping("/assign")
    @Operation(summary = "平台指定俱乐部接单")
    @PreAuthorize("@ss.hasPermission('delta:order-market:assign')")
    public CommonResult<Boolean> assign(@Valid @RequestBody DeltaOrderMarketAssignReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        deltaOrderMarketService.assign(reqVO, operatorId);
        return success(true);
    }
}
