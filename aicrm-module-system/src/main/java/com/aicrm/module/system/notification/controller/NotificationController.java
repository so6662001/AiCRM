package com.aicrm.module.system.notification.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.system.notification.dto.NotificationQueryDTO;
import com.aicrm.module.system.notification.dto.NotificationVO;
import com.aicrm.module.system.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "消息通知")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "分页查询通知")
    public Result<PageResult<NotificationVO>> page(@Valid NotificationQueryDTO query) {
        return Result.ok(notificationService.page(query));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量")
    public Result<Long> unreadCount() {
        return Result.ok(notificationService.unreadCount());
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.ok();
    }
}
