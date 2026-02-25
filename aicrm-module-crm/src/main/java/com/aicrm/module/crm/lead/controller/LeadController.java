package com.aicrm.module.crm.lead.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.lead.dto.*;
import com.aicrm.module.crm.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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

    @PostMapping("/{id}/convert")
    @Operation(summary = "线索转化")
    public Result<Long> convert(@PathVariable Long id, @RequestBody LeadConvertDTO dto) {
        String customerName = dto != null ? dto.getCustomerName() : null;
        boolean createOpportunity = dto != null && Boolean.TRUE.equals(dto.getCreateOpportunity());
        return Result.ok(leadService.convert(id, customerName, createOpportunity));
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入线索")
    public Result<Void> importLeads() {
        return Result.ok();
    }

    @GetMapping("/export")
    @Operation(summary = "导出线索")
    public Result<String> exportLeads() {
        return Result.ok("export_demo.xlsx");
    }

    @GetMapping("/pool")
    @Operation(summary = "公海池线索列表")
    public Result<PageResult<LeadVO>> pool(LeadQueryDTO query) {
        query.setInPool(true);
        return Result.ok(leadService.page(query));
    }

    @PostMapping("/{id}/pick")
    @Operation(summary = "领取线索")
    public Result<Void> pick(@PathVariable Long id) {
        leadService.assign(java.util.List.of(id), TenantContext.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/reassign")
    @Operation(summary = "重新分配线索")
    public Result<Void> reassign(@PathVariable Long id, @Valid @RequestBody ReassignRequest dto) {
        leadService.assign(java.util.List.of(id), dto.getTargetUserId());
        return Result.ok();
    }

    @Data
    public static class ReassignRequest {
        @NotNull(message = "目标用户ID不能为空")
        private Long targetUserId;
    }
}
