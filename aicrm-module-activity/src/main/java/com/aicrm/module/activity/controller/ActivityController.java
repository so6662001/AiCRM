package com.aicrm.module.activity.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.activity.dto.*;
import com.aicrm.module.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动管理控制器
 */
@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "活动CRUD、发布、取消、统计等接口")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "分页查询活动")
    public Result<PageResult<ActivityVO>> page(ActivityQueryDTO query) {
        return Result.ok(activityService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    public Result<ActivityVO> getById(@PathVariable Long id) {
        return Result.ok(activityService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建活动")
    public Result<Long> create(@Valid @RequestBody ActivityCreateDTO dto) {
        return Result.ok(activityService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityUpdateDTO dto) {
        dto.setId(id);
        activityService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "发布活动")
    public Result<Void> publish(@PathVariable Long id) {
        activityService.publish(id);
        return Result.ok();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消活动")
    public Result<Void> cancel(@PathVariable Long id) {
        activityService.cancel(id);
        return Result.ok();
    }

    @PutMapping("/{id}/end")
    @Operation(summary = "结束活动")
    public Result<Void> end(@PathVariable Long id) {
        activityService.end(id);
        return Result.ok();
    }

    @PutMapping("/{id}/close-registration")
    @Operation(summary = "关闭报名")
    public Result<Void> closeRegistration(@PathVariable Long id) {
        activityService.closeRegistration(id);
        return Result.ok();
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取活动统计")
    public Result<ActivityStatisticsVO> getStatistics(@PathVariable Long id) {
        return Result.ok(activityService.getStatistics(id));
    }
}
