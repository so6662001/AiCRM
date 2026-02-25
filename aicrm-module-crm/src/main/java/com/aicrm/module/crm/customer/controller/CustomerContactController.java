package com.aicrm.module.crm.customer.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.customer.dto.ContactCreateDTO;
import com.aicrm.module.crm.customer.dto.ContactVO;
import com.aicrm.module.crm.customer.service.CustomerContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户联系人控制器
 */
@RestController
@RequestMapping("/v1/customers/{customerId}/contacts")
@RequiredArgsConstructor
@Tag(name = "客户联系人管理")
public class CustomerContactController {

    private final CustomerContactService customerContactService;

    @GetMapping
    @Operation(summary = "查询客户联系人列表")
    public Result<List<ContactVO>> list(@PathVariable Long customerId) {
        return Result.ok(customerContactService.list(customerId));
    }

    @PostMapping
    @Operation(summary = "创建联系人")
    public Result<Long> create(@PathVariable Long customerId, @Valid @RequestBody ContactCreateDTO dto) {
        return Result.ok(customerContactService.create(customerId, dto));
    }

    @PutMapping("/{contactId}")
    @Operation(summary = "更新联系人")
    public Result<Void> update(@PathVariable Long customerId, @PathVariable Long contactId,
                               @Valid @RequestBody ContactCreateDTO dto) {
        customerContactService.update(customerId, contactId, dto);
        return Result.ok();
    }

    @DeleteMapping("/{contactId}")
    @Operation(summary = "删除联系人")
    public Result<Void> delete(@PathVariable Long customerId, @PathVariable Long contactId) {
        customerContactService.delete(customerId, contactId);
        return Result.ok();
    }
}
