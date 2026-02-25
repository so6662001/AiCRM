package com.aicrm.module.crm.erp.sync.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.erp.sync.dto.SyncLogDetailVO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogQueryDTO;
import com.aicrm.module.crm.erp.sync.dto.SyncLogVO;
import com.aicrm.module.crm.erp.sync.service.ErpSyncLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ERP同步控制器
 */
@RestController
@RequestMapping("/v1/erp/sync")
@RequiredArgsConstructor
@Tag(name = "ERP同步", description = "ERP同步日志与拉取/推送接口")
public class ErpSyncController {

    private final ErpSyncLogService erpSyncLogService;

    @GetMapping("/logs")
    @Operation(summary = "分页查询同步日志")
    public Result<PageResult<SyncLogVO>> page(@Valid SyncLogQueryDTO query) {
        return Result.ok(erpSyncLogService.page(query));
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "获取同步日志详情")
    public Result<SyncLogDetailVO> getDetail(@PathVariable Long id) {
        return Result.ok(erpSyncLogService.getDetail(id));
    }

    @PostMapping("/customers/pull")
    @Operation(summary = "从ERP拉取客户（DEMO模拟）")
    public Result<Long> pullCustomers() {
        return Result.ok(erpSyncLogService.createMockSync(1, "customer"));
    }

    @PostMapping("/customers/push")
    @Operation(summary = "推送客户到ERP（DEMO模拟）")
    public Result<Long> pushCustomers() {
        return Result.ok(erpSyncLogService.createMockSync(2, "customer"));
    }
}
