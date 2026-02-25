package com.aicrm.module.system.tenant.controller;

import com.aicrm.module.system.tenant.dto.TenantCreateDTO;
import com.aicrm.module.system.tenant.dto.TenantVO;
import com.aicrm.module.system.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器
 */
@RestController
@RequestMapping("/v1/admin/tenants")
@RequiredArgsConstructor
@Tag(name = "租户管理")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "租户列表")
    public List<TenantVO> list() {
        return tenantService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取租户详情")
    public TenantVO getById(@PathVariable Long id) {
        return tenantService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建租户")
    public Long create(@Valid @RequestBody TenantCreateDTO dto) {
        return tenantService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新租户")
    public void update(@PathVariable Long id, @Valid @RequestBody TenantCreateDTO dto) {
        tenantService.update(id, dto);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新租户状态")
    public void updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        tenantService.updateStatus(id, request.getStatus());
    }

    @Data
    @Schema(description = "状态更新请求")
    public static class StatusRequest {
        @NotNull(message = "状态不能为空")
        @Schema(description = "状态 0禁用/1启用/2试用", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer status;
    }
}
