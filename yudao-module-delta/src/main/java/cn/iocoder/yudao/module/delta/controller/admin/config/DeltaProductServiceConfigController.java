package cn.iocoder.yudao.module.delta.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.config.vo.*;
import cn.iocoder.yudao.module.delta.convert.config.DeltaProductServiceConfigConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.config.DeltaProductServiceConfigDO;
import cn.iocoder.yudao.module.delta.service.config.DeltaProductServiceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 商品服务配置 Controller
 */
@Tag(name = "管理后台 - 商品服务配置")
@RestController
@RequestMapping("/delta/product-service-config")
@Validated
public class DeltaProductServiceConfigController {

    @Resource
    private DeltaProductServiceConfigService deltaProductServiceConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建商品服务配置")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody DeltaProductServiceConfigSaveReqVO createReqVO) {
        return success(deltaProductServiceConfigService.createConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品服务配置")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody DeltaProductServiceConfigSaveReqVO updateReqVO) {
        deltaProductServiceConfigService.updateConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品服务配置")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        deltaProductServiceConfigService.deleteConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查询商品服务配置详情")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:query')")
    public CommonResult<DeltaProductServiceConfigRespVO> getConfig(@RequestParam("id") Long id) {
        DeltaProductServiceConfigDO config = deltaProductServiceConfigService.getConfig(id);
        return success(DeltaProductServiceConfigConvert.INSTANCE.convert(config));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询商品服务配置")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:query')")
    public CommonResult<PageResult<DeltaProductServiceConfigRespVO>> getConfigPage(
            @Valid DeltaProductServiceConfigPageReqVO pageReqVO) {
        PageResult<DeltaProductServiceConfigDO> pageResult = deltaProductServiceConfigService.getConfigPage(pageReqVO);
        return success(DeltaProductServiceConfigConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新商品服务配置状态")
    @PreAuthorize("@ss.hasPermission('delta:product-service-config:update')")
    public CommonResult<Boolean> updateConfigStatus(
            @Valid @RequestBody DeltaProductServiceConfigUpdateStatusReqVO reqVO) {
        deltaProductServiceConfigService.updateConfigStatus(reqVO.getId(), reqVO.getEnabled());
        return success(true);
    }

}
