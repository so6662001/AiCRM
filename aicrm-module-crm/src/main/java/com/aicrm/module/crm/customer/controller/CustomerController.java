package com.aicrm.module.crm.customer.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.customer.dto.*;
import com.aicrm.module.crm.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "分页查询客户")
    public Result<PageResult<CustomerVO>> page(@Valid CustomerQueryDTO query) {
        return Result.ok(customerService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    public Result<CustomerVO> getById(@PathVariable Long id) {
        return Result.ok(customerService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建客户")
    public Result<Long> create(@Valid @RequestBody CustomerCreateDTO dto) {
        return Result.ok(customerService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        dto.setId(id);
        customerService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok();
    }

    @PostMapping("/transfer")
    @Operation(summary = "批量转移客户")
    public Result<Void> transfer(@Valid @RequestBody CustomerTransferRequest request) {
        customerService.transfer(request.getCustomerIds(), request.getTargetUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/mark-invalid")
    @Operation(summary = "标记客户无效")
    public Result<Void> markInvalid(@PathVariable Long id, @RequestBody MarkInvalidRequest request) {
        customerService.markInvalid(id, request != null ? request.getReason() : null);
        return Result.ok();
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "重新激活客户")
    public Result<Void> reactivate(@PathVariable Long id) {
        customerService.reactivate(id);
        return Result.ok();
    }

    @Data
    @Schema(description = "客户转移请求")
    public static class CustomerTransferRequest {
        @NotEmpty(message = "客户ID列表不能为空")
        private List<Long> customerIds;
        @NotNull(message = "目标负责人不能为空")
        private Long targetUserId;
    }

    @Data
    @Schema(description = "标记无效请求")
    public static class MarkInvalidRequest {
        private String reason;
    }
}
