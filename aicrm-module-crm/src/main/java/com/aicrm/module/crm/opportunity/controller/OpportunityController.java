package com.aicrm.module.crm.opportunity.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.opportunity.dto.*;
import com.aicrm.module.crm.opportunity.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/opportunities")
@RequiredArgsConstructor
@Tag(name = "商机管理")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    @Operation(summary = "分页查询商机")
    public Result<PageResult<OpportunityVO>> page(@Valid OpportunityQueryDTO query) {
        return Result.ok(opportunityService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商机详情")
    public Result<OpportunityVO> getById(@PathVariable Long id) {
        return Result.ok(opportunityService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建商机")
    public Result<Long> create(@Valid @RequestBody OpportunityCreateDTO dto) {
        return Result.ok(opportunityService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商机")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OpportunityUpdateDTO dto) {
        dto.setId(id);
        opportunityService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商机")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/stage")
    @Operation(summary = "变更商机阶段")
    public Result<Void> changeStage(@PathVariable Long id, @Valid @RequestBody StageChangeDTO dto) {
        opportunityService.changeStage(id, dto);
        return Result.ok();
    }

    @PostMapping("/{id}/win")
    @Operation(summary = "赢单")
    public Result<Void> win(@PathVariable Long id, @RequestBody WinRequest request) {
        opportunityService.win(id, request.getActualAmount());
        return Result.ok();
    }

    @PostMapping("/{id}/lose")
    @Operation(summary = "输单")
    public Result<Void> lose(@PathVariable Long id, @RequestBody LoseRequest request) {
        opportunityService.lose(id, request.getLossReason());
        return Result.ok();
    }

    @Data
    @Schema(description = "赢单请求")
    public static class WinRequest {
        private BigDecimal actualAmount;
    }

    @Data
    @Schema(description = "输单请求")
    public static class LoseRequest {
        private String lossReason;
    }
}
