package com.aicrm.module.crm.opportunity.controller;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.opportunity.dto.*;
import com.aicrm.module.crm.opportunity.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商机管理控制器
 */
@RestController
@RequestMapping("/v1/opportunities")
@RequiredArgsConstructor
@Tag(name = "商机管理")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    @Operation(summary = "分页查询商机")
    public PageResult<OpportunityVO> page(@Valid OpportunityQueryDTO query) {
        return opportunityService.page(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商机详情")
    public OpportunityVO getById(@PathVariable Long id) {
        return opportunityService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建商机")
    public Long create(@Valid @RequestBody OpportunityCreateDTO dto) {
        return opportunityService.create(dto);
    }

    @PutMapping
    @Operation(summary = "更新商机")
    public void update(@Valid @RequestBody OpportunityUpdateDTO dto) {
        opportunityService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商机")
    public void delete(@PathVariable Long id) {
        opportunityService.delete(id);
    }

    @PutMapping("/{id}/stage")
    @Operation(summary = "变更商机阶段")
    public void changeStage(@PathVariable Long id, @Valid @RequestBody StageChangeDTO dto) {
        opportunityService.changeStage(id, dto);
    }

    @PostMapping("/{id}/win")
    @Operation(summary = "赢单")
    public void win(@PathVariable Long id, @Valid @RequestBody WinRequest request) {
        opportunityService.win(id, request.getActualAmount());
    }

    @PostMapping("/{id}/lose")
    @Operation(summary = "输单")
    public void lose(@PathVariable Long id, @Valid @RequestBody LoseRequest request) {
        opportunityService.lose(id, request.getLossReason());
    }

    @lombok.Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "赢单请求")
    public static class WinRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "实际成交金额")
        private BigDecimal actualAmount;
    }

    @lombok.Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "输单请求")
    public static class LoseRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "输单原因")
        private String lossReason;
    }
}
