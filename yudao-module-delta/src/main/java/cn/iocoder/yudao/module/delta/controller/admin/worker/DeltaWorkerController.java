package cn.iocoder.yudao.module.delta.controller.admin.worker;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.delta.controller.admin.worker.vo.*;
import cn.iocoder.yudao.module.delta.convert.worker.DeltaWorkerConvert;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerDO;
import cn.iocoder.yudao.module.delta.dal.dataobject.worker.DeltaWorkerSkillDO;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerService;
import cn.iocoder.yudao.module.delta.service.worker.DeltaWorkerSkillService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - 打手管理
 *
 * @author Delta-Vanguard
 */
@Tag(name = "管理后台 - 打手管理")
@RestController
@RequestMapping("/delta/worker")
@Validated
@Slf4j
public class DeltaWorkerController {

    @Resource
    private DeltaWorkerService deltaWorkerService;

    @Resource
    private DeltaWorkerSkillService deltaWorkerSkillService;

    @Resource
    private MemberUserApi memberUserApi;

    // ===================== 分页查询 =====================

    @GetMapping("/page")
    @Operation(summary = "获得打手分页列表")
    @PreAuthorize("@ss.hasPermission('delta:worker:query')")
    public CommonResult<PageResult<DeltaWorkerRespVO>> getWorkerPage(@Valid DeltaWorkerPageReqVO pageReqVO) {
        PageResult<DeltaWorkerDO> pageResult = deltaWorkerService.getWorkerPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 批量查询会员信息（反 N+1）
        Set<Long> userIds = pageResult.getList().stream()
                .map(DeltaWorkerDO::getUserId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberUserMap = memberUserApi.getUserMap(userIds);
        return success(DeltaWorkerConvert.INSTANCE.convertPage2(pageResult, memberUserMap));
    }

    // ===================== 详情查询 =====================

    @GetMapping("/get")
    @Operation(summary = "获得打手详情")
    @Parameter(name = "id", description = "打手ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('delta:worker:query')")
    public CommonResult<DeltaWorkerRespVO> getWorker(@RequestParam("id") Long id) {
        DeltaWorkerDO worker = deltaWorkerService.getWorker(id);
        if (worker == null) {
            return success(null);
        }
        // 查询会员信息
        MemberUserRespDTO memberUser = memberUserApi.getUser(worker.getUserId());
        DeltaWorkerRespVO respVO = DeltaWorkerConvert.INSTANCE.convert2(worker, memberUser);
        // 查询技能
        List<DeltaWorkerSkillDO> skills = deltaWorkerSkillService.getSkillListByWorkerId(id);
        respVO.setSkills(DeltaWorkerConvert.INSTANCE.convertSkillList2(skills));
        // 构建技能摘要
        if (CollUtil.isNotEmpty(skills)) {
            respVO.setSkillSummary(buildSkillSummary(skills));
        }
        return success(respVO);
    }

    // ===================== 修改 =====================

    @PutMapping("/update")
    @Operation(summary = "修改打手资料")
    @PreAuthorize("@ss.hasPermission('delta:worker:update')")
    public CommonResult<Boolean> updateWorker(@RequestBody @Valid DeltaWorkerUpdateReqVO reqVO) {
        deltaWorkerService.updateWorker(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改打手启用状态")
    @PreAuthorize("@ss.hasPermission('delta:worker:update-status')")
    public CommonResult<Boolean> updateWorkerStatus(@RequestBody @Valid DeltaWorkerStatusUpdateReqVO reqVO) {
        deltaWorkerService.updateWorkerStatus(reqVO.getId(), reqVO.getStatus(), reqVO.getReason());
        return success(true);
    }

    @PutMapping("/update-skills")
    @Operation(summary = "更新打手技能")
    @PreAuthorize("@ss.hasPermission('delta:worker:update-skill')")
    public CommonResult<Boolean> updateWorkerSkills(@RequestBody @Valid DeltaWorkerSkillUpdateReqVO reqVO) {
        // 校验打手存在
        deltaWorkerService.getWorker(reqVO.getWorkerId());
        // 构建技能 DO 列表
        List<DeltaWorkerSkillDO> skills = reqVO.getSkills().stream().map(item -> {
            DeltaWorkerSkillDO skill = new DeltaWorkerSkillDO();
            skill.setDeviceType(item.getDeviceType());
            skill.setServiceType(item.getServiceType());
            skill.setSkillLevel(item.getSkillLevel());
            skill.setStatus(item.getStatus() != null ? item.getStatus() : 0);
            return skill;
        }).collect(Collectors.toList());
        deltaWorkerSkillService.replaceSkills(reqVO.getWorkerId(), skills);
        return success(true);
    }

    // ===================== 私有方法 =====================

    private String buildSkillSummary(List<DeltaWorkerSkillDO> skills) {
        return skills.stream().map(s -> {
            String device = deviceTypeName(s.getDeviceType());
            String service = serviceTypeName(s.getServiceType());
            String level = levelName(s.getSkillLevel());
            return device + service + "(" + level + ")";
        }).collect(Collectors.joining(", "));
    }

    private String deviceTypeName(Integer type) {
        switch (type) {
            case 1: return "手机";
            case 2: return "平板";
            case 3: return "PC";
            default: return "未知";
        }
    }

    private String serviceTypeName(Integer type) {
        switch (type) {
            case 1: return "陪玩";
            case 2: return "护航";
            case 3: return "趣味单";
            default: return "未知";
        }
    }

    private String levelName(Integer level) {
        switch (level) {
            case 1: return "初级";
            case 2: return "中级";
            case 3: return "高级";
            case 4: return "资深";
            default: return "未知";
        }
    }

}
