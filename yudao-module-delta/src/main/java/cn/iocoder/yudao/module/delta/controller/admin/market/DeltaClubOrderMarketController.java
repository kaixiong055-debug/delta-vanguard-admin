package cn.iocoder.yudao.module.delta.controller.admin.market;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.market.vo.*;
import cn.iocoder.yudao.module.delta.convert.market.DeltaOrderMarketConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
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
 * 俱乐部订单市场 Controller（俱乐部视角接口）
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 俱乐部订单市场")
@RestController
@RequestMapping("/delta/order-market")
@Validated
public class DeltaClubOrderMarketController {

    @Resource
    private DeltaOrderMarketService deltaOrderMarketService;

    // ========== 可抢订单 ==========

    @GetMapping("/available-page")
    @Operation(summary = "可抢订单分页")
    @PreAuthorize("@ss.hasPermission('delta:order-market:claim')")
    public CommonResult<PageResult<DeltaOrderMarketListingRespVO>> availablePage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<DeltaOrderMarketListingDO> page = deltaOrderMarketService.getAvailablePage(pageNo, pageSize);
        PageResult<DeltaOrderMarketListingRespVO> result = DeltaOrderMarketConvert.INSTANCE.convertPage(page);
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(result.getList());
        return success(result);
    }

    @GetMapping("/available-get")
    @Operation(summary = "可抢订单详情")
    @PreAuthorize("@ss.hasPermission('delta:order-market:claim')")
    public CommonResult<DeltaOrderMarketListingRespVO> availableGet(@RequestParam("id") Long id) {
        DeltaOrderMarketListingDO listing = deltaOrderMarketService.getAvailable(id);
        DeltaOrderMarketListingRespVO vo = DeltaOrderMarketConvert.INSTANCE.convert(listing);
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(Collections.singletonList(vo));
        return success(vo);
    }

    @PostMapping("/claim")
    @Operation(summary = "俱乐部抢单")
    @PreAuthorize("@ss.hasPermission('delta:order-market:claim')")
    public CommonResult<Boolean> claim(@Valid @RequestBody DeltaOrderMarketClaimReqVO reqVO) {
        deltaOrderMarketService.claim(reqVO.getId());
        return success(true);
    }

    // ========== 已接订单 ==========

    @GetMapping("/my-claimed-page")
    @Operation(summary = "我的已接订单分页")
    @PreAuthorize("@ss.hasPermission('delta:order-market:query')")
    public CommonResult<PageResult<DeltaOrderMarketListingRespVO>> myClaimedPage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<DeltaOrderMarketListingDO> page = deltaOrderMarketService.getMyClaimedPage(pageNo, pageSize);
        PageResult<DeltaOrderMarketListingRespVO> result = DeltaOrderMarketConvert.INSTANCE.convertPage(page);
        DeltaOrderMarketConvert.INSTANCE.fillStatusAndServiceTypeNames(result.getList());
        return success(result);
    }
}
