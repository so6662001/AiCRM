package com.aicrm.module.crm.opportunity.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.opportunity.dto.StageConfigDTO;
import com.aicrm.module.crm.opportunity.dto.StageConfigVO;
import com.aicrm.module.crm.opportunity.service.OpportunityStageConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商机阶段配置控制器
 */
@RestController
@RequestMapping("/v1/opportunity-stage-configs")
@RequiredArgsConstructor
@Tag(name = "商机阶段配置")
public class OpportunityStageConfigController {

    private final OpportunityStageConfigService opportunityStageConfigService;

    @GetMapping
    @Operation(summary = "阶段配置列表")
    public Result<List<StageConfigVO>> list() {
        return Result.ok(opportunityStageConfigService.list());
    }

    @PostMapping
    @Operation(summary = "创建阶段配置")
    public Result<Long> create(@Valid @RequestBody StageConfigDTO dto) {
        return Result.ok(opportunityStageConfigService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新阶段配置")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StageConfigDTO dto) {
        opportunityStageConfigService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除阶段配置")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityStageConfigService.delete(id);
        return Result.ok();
    }

    @PutMapping("/sort")
    @Operation(summary = "排序阶段配置")
    public Result<Void> sort(@RequestBody List<Long> ids) {
        opportunityStageConfigService.sort(ids);
        return Result.ok();
    }
}
