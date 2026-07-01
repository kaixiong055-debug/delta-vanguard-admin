package cn.iocoder.yudao.module.delta.controller.app.notification;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.delta.controller.app.notification.vo.*;
import cn.iocoder.yudao.module.delta.dal.dataobject.event.DeltaMemberNotificationDO;
import cn.iocoder.yudao.module.delta.service.event.DeltaMemberNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App - 会员站内通知 Controller
 */
@Tag(name = "用户 App - 站内通知")
@RestController
@RequestMapping("/delta/notification")
@Validated
public class AppDeltaNotificationController {

    @Resource
    private DeltaMemberNotificationService deltaMemberNotificationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通知")
    public CommonResult<PageResult<AppDeltaNotificationRespVO>> getNotificationPage(
            @Valid AppDeltaNotificationPageReqVO pageReqVO) {
        Long userId = getLoginUserId();
        Long tenantId = TenantContextHolder.getTenantId();

        LocalDateTime[] createTime = null;
        if (pageReqVO.getCreateTimeStart() != null && pageReqVO.getCreateTimeEnd() != null) {
            createTime = new LocalDateTime[]{pageReqVO.getCreateTimeStart(), pageReqVO.getCreateTimeEnd()};
        }

        PageResult<DeltaMemberNotificationDO> pageResult = deltaMemberNotificationService.getNotificationPage(
                userId, tenantId, pageReqVO, pageReqVO.getReadStatus(),
                pageReqVO.getNotificationType(), createTime);

        PageResult<AppDeltaNotificationRespVO> result = new PageResult<>(
                pageResult.getList().stream().map(AppDeltaNotificationRespVO::from).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "查询通知详情")
    public CommonResult<AppDeltaNotificationRespVO> getNotification(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        DeltaMemberNotificationDO notification = deltaMemberNotificationService.getNotification(id, userId, tenantId);
        return success(AppDeltaNotificationRespVO.from(notification));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "查询未读数量")
    public CommonResult<Long> getUnreadCount() {
        Long userId = getLoginUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        Long count = deltaMemberNotificationService.getUnreadCount(userId, tenantId);
        return success(count);
    }

    @PutMapping("/read")
    @Operation(summary = "标记已读")
    public CommonResult<Boolean> markRead(@RequestBody @Valid AppDeltaNotificationReadReqVO reqVO) {
        Long userId = getLoginUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        deltaMemberNotificationService.markRead(reqVO.getId(), userId, tenantId);
        return success(true);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public CommonResult<Boolean> markAllRead() {
        Long userId = getLoginUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        deltaMemberNotificationService.markAllRead(userId, tenantId);
        return success(true);
    }
}
