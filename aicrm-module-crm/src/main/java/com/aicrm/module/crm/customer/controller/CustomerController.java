package com.aicrm.module.crm.customer.controller;

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

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "分页查询客户")
    public PageResult<CustomerVO> page(@Valid CustomerQueryDTO query) {
        return customerService.page(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    public CustomerVO getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建客户")
    public Long create(@Valid @RequestBody CustomerCreateDTO dto) {
        return customerService.create(dto);
    }

    @PutMapping
    @Operation(summary = "更新客户")
    public void update(@Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }

    @PostMapping("/transfer")
    @Operation(summary = "批量转移客户")
    public void transfer(@Valid @RequestBody CustomerTransferRequest request) {
        customerService.transfer(request.getCustomerIds(), request.getTargetUserId());
    }

    @PostMapping("/{id}/mark-invalid")
    @Operation(summary = "标记客户无效")
    public void markInvalid(@PathVariable Long id, @Valid @RequestBody MarkInvalidRequest request) {
        customerService.markInvalid(id, request.getReason());
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "重新激活客户")
    public void reactivate(@PathVariable Long id) {
        customerService.reactivate(id);
    }

    @Data
    @Schema(description = "客户转移请求")
    public static class CustomerTransferRequest {
        @NotEmpty(message = "客户ID列表不能为空")
        @Schema(description = "客户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> customerIds;
        @NotNull(message = "目标负责人不能为空")
        @Schema(description = "目标负责人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long targetUserId;
    }

    @Data
    @Schema(description = "标记无效请求")
    public static class MarkInvalidRequest {
        @Schema(description = "无效原因")
        private String reason;
    }
}
