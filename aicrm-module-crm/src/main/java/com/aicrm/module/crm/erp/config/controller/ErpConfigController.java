package com.aicrm.module.crm.erp.config.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.erp.config.dto.ErpConfigDTO;
import com.aicrm.module.crm.erp.config.dto.ErpConfigVO;
import com.aicrm.module.crm.erp.config.service.ErpConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ERP配置管理控制器
 */
@RestController
@RequestMapping("/v1/erp-configs")
@RequiredArgsConstructor
@Tag(name = "ERP配置管理")
public class ErpConfigController {

    private final ErpConfigService erpConfigService;

    @GetMapping
    @Operation(summary = "ERP配置列表")
    public Result<List<ErpConfigVO>> list() {
        return Result.ok(erpConfigService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "ERP配置详情")
    public Result<ErpConfigVO> getById(@PathVariable Long id) {
        return Result.ok(erpConfigService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建ERP配置")
    public Result<Long> create(@Valid @RequestBody ErpConfigDTO dto) {
        return Result.ok(erpConfigService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新ERP配置")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ErpConfigDTO dto) {
        erpConfigService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除ERP配置")
    public Result<Void> delete(@PathVariable Long id) {
        erpConfigService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试连接")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        return Result.ok(erpConfigService.testConnection(id));
    }
}
