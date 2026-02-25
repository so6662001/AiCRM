package com.aicrm.module.system.org.controller;

import com.aicrm.module.system.org.dto.OrgCreateDTO;
import com.aicrm.module.system.org.dto.OrgVO;
import com.aicrm.module.system.org.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构管理控制器
 */
@RestController
@RequestMapping("/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "组织架构管理")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/tree")
    @Operation(summary = "获取组织树")
    public List<OrgVO> tree() {
        return organizationService.tree();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取组织详情")
    public OrgVO getById(@PathVariable Long id) {
        return organizationService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建组织")
    public Long create(@Valid @RequestBody OrgCreateDTO dto) {
        return organizationService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新组织")
    public void update(@PathVariable Long id, @Valid @RequestBody OrgCreateDTO dto) {
        organizationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织")
    public void delete(@PathVariable Long id) {
        organizationService.delete(id);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取组织成员列表")
    public List<Object> listMembers(@PathVariable Long id) {
        return organizationService.listMembers(id);
    }
}
