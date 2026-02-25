package com.aicrm.module.crm.lead.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.lead.dto.*;
import com.aicrm.module.crm.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 线索管理控制器
 */
@RestController
@RequestMapping("/v1/leads")
@RequiredArgsConstructor
@Tag(name = "线索管理", description = "线索CRUD、分配、退回等接口")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    @Operation(summary = "分页查询线索")
    public Result<PageResult<LeadVO>> page(LeadQueryDTO query) {
        return Result.ok(leadService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取线索详情")
    public Result<LeadVO> getById(@PathVariable Long id) {
        return Result.ok(leadService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建线索")
    public Result<Long> create(@Valid @RequestBody LeadCreateDTO dto) {
        return Result.ok(leadService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新线索")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody LeadUpdateDTO dto) {
        dto.setId(id);
        leadService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除线索")
    public Result<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return Result.ok();
    }

    @PostMapping("/assign")
    @Operation(summary = "分配线索")
    public Result<Void> assign(@Valid @RequestBody LeadAssignDTO dto) {
        leadService.assign(dto.getLeadIds(), dto.getTargetUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "退回线索到公海池")
    public Result<Void> returnToPool(@PathVariable Long id, @RequestBody LeadReturnDTO dto) {
        leadService.returnToPool(id, dto != null ? dto.getReturnReason() : null);
        return Result.ok();
    }
}
