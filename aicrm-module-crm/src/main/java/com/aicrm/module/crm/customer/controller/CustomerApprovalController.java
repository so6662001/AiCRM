package com.aicrm.module.crm.customer.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.customer.dto.ApprovalVO;
import com.aicrm.module.crm.customer.service.CustomerApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户审核控制器
 */
@RestController
@RequestMapping("/v1/customer-approvals")
@RequiredArgsConstructor
@Tag(name = "客户审核")
public class CustomerApprovalController {

    private final CustomerApprovalService customerApprovalService;

    @GetMapping
    @Operation(summary = "查询审核列表")
    public Result<List<ApprovalVO>> list() {
        return Result.ok(customerApprovalService.list());
    }

    @PostMapping("/{customerId}/submit")
    @Operation(summary = "提交审核")
    public Result<Long> submitApproval(@PathVariable Long customerId, @RequestBody Map<String, Integer> body) {
        Integer type = body != null ? body.get("type") : null;
        if (type == null) {
            type = 1;
        }
        return Result.ok(customerApprovalService.submitApproval(customerId, type));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        customerApprovalService.approve(id, remark);
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        customerApprovalService.reject(id, remark);
        return Result.ok();
    }
}
