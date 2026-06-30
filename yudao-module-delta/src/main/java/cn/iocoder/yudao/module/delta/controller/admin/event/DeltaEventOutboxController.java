package cn.iocoder.yudao.module.delta.controller.admin.event;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.delta.controller.admin.event.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaEventOutboxDO;
import cn.iocoder.yudao.module.delta.dal.mysql.event.DeltaEventOutboxMapper;
import cn.iocoder.yudao.module.delta.enums.event.EventOutboxStatusEnum;
import cn.iocoder.yudao.module.delta.service.event.DeltaEventOutboxConsumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.delta.enums.ErrorCodeConstants.*;

/**
 * Admin - Outbox 事件管理 Controller
 */
@Tag(name = "管理后台 - Outbox 事件管理")
@RestController
@RequestMapping("/delta/event-outbox")
@Validated
public class DeltaEventOutboxController {

    @Resource
    private DeltaEventOutboxMapper deltaEventOutboxMapper;
    @Resource
    private DeltaEventOutboxConsumeService deltaEventOutboxConsumeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询 Outbox 事件")
    @PreAuthorize("@ss.hasPermission('delta:event-outbox:query')")
    public CommonResult<PageResult<DeltaEventOutboxRespVO>> getEventOutboxPage(
            @Valid DeltaEventOutboxPageReqVO pageReqVO) {
        PageResult<DeltaEventOutboxDO> pageResult = deltaEventOutboxMapper.selectPage(pageReqVO);
        List<DeltaEventOutboxRespVO> voList = pageResult.getList().stream()
                .map(DeltaEventOutboxRespVO::from).collect(Collectors.toList());
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "查询 Outbox 事件详情")
    @PreAuthorize("@ss.hasPermission('delta:event-outbox:query')")
    public CommonResult<DeltaEventOutboxRespVO> getEventOutboxDetail(@RequestParam("id") Long id) {
        DeltaEventOutboxDO event = deltaEventOutboxMapper.selectById(id);
        if (event == null) {
            throw exception(EVENT_OUTBOX_NOT_EXISTS);
        }
        return success(DeltaEventOutboxRespVO.from(event));
    }

    @PostMapping("/retry")
    @Operation(summary = "人工重试 Outbox 事件")
    @PreAuthorize("@ss.hasPermission('delta:event-outbox:retry')")
    public CommonResult<Boolean> retryEvent(@RequestBody @Valid DeltaEventOutboxRetryReqVO reqVO) {
        Long eventId = reqVO.getId();
        DeltaEventOutboxDO event = deltaEventOutboxMapper.selectById(eventId);
        if (event == null) {
            throw exception(EVENT_OUTBOX_NOT_EXISTS);
        }
        if (!EventOutboxStatusEnum.canRetry(event.getEventStatus())) {
            throw exception(EVENT_OUTBOX_STATUS_CANNOT_RETRY);
        }
        // CAS: FAILED -> PENDING
        int rows = deltaEventOutboxMapper.casRetry(eventId);
        if (rows != 1) {
            throw exception(EVENT_OUTBOX_STATUS_CHANGED);
        }
        return success(true);
    }

    @PostMapping("/mark-dead")
    @Operation(summary = "标记 Outbox 事件为死亡")
    @PreAuthorize("@ss.hasPermission('delta:event-outbox:update')")
    public CommonResult<Boolean> markDead(@RequestBody @Valid DeltaEventOutboxMarkDeadReqVO reqVO) {
        Long eventId = reqVO.getId();
        DeltaEventOutboxDO event = deltaEventOutboxMapper.selectById(eventId);
        if (event == null) {
            throw exception(EVENT_OUTBOX_NOT_EXISTS);
        }
        if (!EventOutboxStatusEnum.canMarkDead(event.getEventStatus())) {
            throw exception(EVENT_OUTBOX_STATUS_CANNOT_MARK_DEAD);
        }
        int rows;
        if (EventOutboxStatusEnum.PENDING.getStatus().equals(event.getEventStatus())) {
            rows = deltaEventOutboxMapper.casMarkDeadFromPending(eventId);
        } else {
            rows = deltaEventOutboxMapper.casMarkDead(eventId);
        }
        if (rows != 1) {
            throw exception(EVENT_OUTBOX_STATUS_CHANGED);
        }
        return success(true);
    }
}
