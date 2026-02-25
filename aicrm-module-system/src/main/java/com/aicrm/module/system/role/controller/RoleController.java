package com.aicrm.module.system.role.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.system.role.dto.RoleCreateDTO;
import com.aicrm.module.system.role.dto.RoleVO;
import com.aicrm.module.system.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "角色列表")
    public Result<List<RoleVO>> list() {
        return Result.ok(roleService.list());
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.ok(roleService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleCreateDTO dto) {
        roleService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限")
    public Result<List<String>> getPermissions(@PathVariable Long id) {
        return Result.ok(roleService.getPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "更新角色权限")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody List<String> codes) {
        roleService.updatePermissions(id, codes);
        return Result.ok();
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "分配用户角色")
    public Result<Void> assignUserRole(@PathVariable Long userId, @RequestBody AssignRoleRequest request) {
        roleService.assignUserRole(userId, request.getRoleId(), request.getOrgId());
        return Result.ok();
    }

    @lombok.Data
    public static class AssignRoleRequest {
        private Long roleId;
        private Long orgId;
    }
}
