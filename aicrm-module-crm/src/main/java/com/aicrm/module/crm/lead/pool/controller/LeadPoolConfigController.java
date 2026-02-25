package com.aicrm.module.crm.lead.pool.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.lead.pool.dto.PoolConfigDTO;
import com.aicrm.module.crm.lead.pool.dto.PoolConfigVO;
import com.aicrm.module.crm.lead.pool.service.LeadPoolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公海池配置控制器
 */
@RestController
@RequestMapping("/v1/lead-pool-configs")
@RequiredArgsConstructor
@Tag(name = "公海池配置", description = "公海池配置管理接口")
public class LeadPoolConfigController {

    private final LeadPoolConfigService leadPoolConfigService;

    @GetMapping
    @Operation(summary = "查询公海池配置列表")
    public Result<List<PoolConfigVO>> list() {
        return Result.ok(leadPoolConfigService.list());
    }

    @PostMapping
    @Operation(summary = "创建公海池配置")
    public Result<Long> create(@Valid @RequestBody PoolConfigDTO dto) {
        return Result.ok(leadPoolConfigService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公海池配置")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PoolConfigDTO dto) {
        leadPoolConfigService.update(id, dto);
        return Result.ok();
    }
}
