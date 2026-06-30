package cn.iocoder.yudao.module.delta.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.club.vo.*;
import cn.iocoder.yudao.module.delta.convert.club.DeltaClubConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.club.DeltaClubProfileDO;
import cn.iocoder.yudao.module.delta.service.club.DeltaClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 俱乐部管理
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 俱乐部管理")
@RestController
@RequestMapping("/delta/club")
@Validated
@Slf4j
public class DeltaClubController {

    @Resource
    private DeltaClubService deltaClubService;

    // ===================== 分页查询 =====================

    @GetMapping("/page")
    @Operation(summary = "获得俱乐部分页列表")
    @PreAuthorize("@ss.hasPermission('delta:club:query')")
    public CommonResult<PageResult<DeltaClubRespVO>> getClubPage(@Valid DeltaClubPageReqVO pageReqVO) {
        PageResult<DeltaClubProfileDO> pageResult = deltaClubService.getClubPage(pageReqVO);
        return success(DeltaClubConvert.INSTANCE.convertPage(pageResult));
    }

    // ===================== 详情查询 =====================

    @GetMapping("/get")
    @Operation(summary = "获得俱乐部详情")
    @Parameter(name = "id", description = "俱乐部ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('delta:club:query')")
    public CommonResult<DeltaClubRespVO> getClub(@RequestParam("id") Long id) {
        return success(deltaClubService.getClub(id));
    }

    @GetMapping("/get-current")
    @Operation(summary = "获得当前租户俱乐部详情")
    @PreAuthorize("@ss.hasPermission('delta:club:query')")
    public CommonResult<DeltaClubRespVO> getCurrentClub() {
        return success(deltaClubService.getCurrentClub());
    }

    // ===================== 更新操作 =====================

    @PutMapping("/update")
    @Operation(summary = "更新俱乐部档案")
    @PreAuthorize("@ss.hasPermission('delta:club:update')")
    public CommonResult<Boolean> updateClub(@RequestBody @Valid DeltaClubUpdateReqVO reqVO) {
        deltaClubService.updateClub(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新俱乐部经营状态")
    @PreAuthorize("@ss.hasPermission('delta:club:update')")
    public CommonResult<Boolean> updateClubStatus(@RequestBody @Valid DeltaClubUpdateStatusReqVO reqVO) {
        deltaClubService.updateClubStatus(reqVO);
        return success(true);
    }

    @PutMapping("/update-service-scope")
    @Operation(summary = "更新俱乐部服务范围")
    @PreAuthorize("@ss.hasPermission('delta:club:update')")
    public CommonResult<Boolean> updateClubServiceScope(@RequestBody @Valid DeltaClubUpdateServiceScopeReqVO reqVO) {
        deltaClubService.updateClubServiceScope(reqVO);
        return success(true);
    }

}
