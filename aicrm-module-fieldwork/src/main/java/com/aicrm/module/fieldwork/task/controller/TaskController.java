package com.aicrm.module.fieldwork.task.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.task.dto.TaskCreateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskQueryDTO;
import com.aicrm.module.fieldwork.task.dto.TaskUpdateDTO;
import com.aicrm.module.fieldwork.task.dto.TaskVO;
import com.aicrm.module.fieldwork.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 任务管理Controller
 */
@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "分页查询任务")
    public Result<PageResult<TaskVO>> page(@Valid TaskQueryDTO query) {
        return Result.ok(taskService.page(query));
    }

    @GetMapping("/today/summary")
    @Operation(summary = "今日任务汇总")
    public Result<Map<String, Object>> todaySummary() {
        return Result.ok(taskService.todaySummary());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    public Result<TaskVO> getById(@PathVariable Long id) {
        return Result.ok(taskService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建任务")
    public Result<Long> create(@Valid @RequestBody TaskCreateDTO dto) {
        return Result.ok(taskService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新任务")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO dto) {
        dto.setId(id);
        taskService.update(dto);
        return Result.ok();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成任务")
    public Result<Void> complete(@PathVariable Long id, @RequestBody CompleteRequest request) {
        taskService.complete(id, request != null ? request.getCompletionNote() : null);
        return Result.ok();
    }

    @Data
    public static class CompleteRequest {
        private String completionNote;
    }
}
