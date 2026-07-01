package cn.iocoder.yudao.module.delta.controller.app.market;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketClaimReqVO;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketPageReqVO;
import cn.iocoder.yudao.module.delta.controller.app.market.vo.AppDeltaOrderMarketRespVO;
import cn.iocoder.yudao.module.delta.convert.market.AppDeltaOrderMarketConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.market.DeltaOrderMarketListingDO;
import cn.iocoder.yudao.module.delta.service.market.DeltaOrderMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 俱乐部订单市场")
@RestController
@RequestMapping("/delta/order-market")
@Validated
public class AppDeltaOrderMarketController {

    @Resource
    private DeltaOrderMarketService deltaOrderMarketService;

    @GetMapping("/available-page")
    @Operation(summary = "分页查询当前会员俱乐部可抢挂牌")
    public CommonResult<PageResult<AppDeltaOrderMarketRespVO>> getAvailablePage(
            @Valid AppDeltaOrderMarketPageReqVO pageReqVO) {
        PageResult<DeltaOrderMarketListingDO> page =
                deltaOrderMarketService.getAvailablePageForMember(
                        getLoginUserId(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        return success(AppDeltaOrderMarketConvert.INSTANCE.convertPage(page));
    }

    @GetMapping("/available-get")
    @Operation(summary = "查询当前会员俱乐部可抢挂牌详情")
    public CommonResult<AppDeltaOrderMarketRespVO> getAvailable(
            @RequestParam("id") Long id) {
        DeltaOrderMarketListingDO listing =
                deltaOrderMarketService.getAvailableForMember(getLoginUserId(), id);
        return success(AppDeltaOrderMarketConvert.INSTANCE.convert(listing));
    }

    @PostMapping("/claim")
    @Operation(summary = "当前会员拥有的俱乐部抢单")
    public CommonResult<Boolean> claim(
            @RequestBody @Valid AppDeltaOrderMarketClaimReqVO reqVO) {
        deltaOrderMarketService.claimForMember(getLoginUserId(), reqVO.getId());
        return success(true);
    }

    @GetMapping("/my-claimed-page")
    @Operation(summary = "分页查询当前会员俱乐部已接挂牌")
    public CommonResult<PageResult<AppDeltaOrderMarketRespVO>> getMyClaimedPage(
            @Valid AppDeltaOrderMarketPageReqVO pageReqVO) {
        PageResult<DeltaOrderMarketListingDO> page =
                deltaOrderMarketService.getMyClaimedPageForMember(
                        getLoginUserId(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        return success(AppDeltaOrderMarketConvert.INSTANCE.convertPage(page));
    }
}
