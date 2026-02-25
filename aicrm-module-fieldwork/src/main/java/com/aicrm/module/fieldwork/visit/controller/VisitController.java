package com.aicrm.module.fieldwork.visit.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.visit.dto.VisitCreateDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitQueryDTO;
import com.aicrm.module.fieldwork.visit.dto.VisitVO;
import com.aicrm.module.fieldwork.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 拜访管理Controller
 */
@RestController
@RequestMapping("/v1/visits")
@RequiredArgsConstructor
@Tag(name = "拜访管理")
public class VisitController {

    private final VisitService visitService;

    @GetMapping
    @Operation(summary = "分页查询拜访记录")
    public Result<PageResult<VisitVO>> page(@Valid VisitQueryDTO query) {
        return Result.ok(visitService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取拜访详情")
    public Result<VisitVO> getById(@PathVariable Long id) {
        return Result.ok(visitService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建拜访")
    public Result<Long> create(@Valid @RequestBody VisitCreateDTO dto) {
        return Result.ok(visitService.create(dto));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成拜访")
    public Result<Void> complete(@PathVariable Long id, @RequestBody CompleteRequest request) {
        visitService.complete(id, request != null ? request.getVisitResult() : null);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消拜访")
    public Result<Void> cancel(@PathVariable Long id) {
        visitService.cancel(id);
        return Result.ok();
    }

    @Data
    public static class CompleteRequest {
        private String visitResult;
    }
}
