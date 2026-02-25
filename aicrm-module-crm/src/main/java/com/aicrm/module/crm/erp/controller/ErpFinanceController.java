package com.aicrm.module.crm.erp.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.erp.dto.ErpReceivableVO;
import com.aicrm.module.crm.erp.dto.ErpTransactionVO;
import com.aicrm.module.crm.erp.service.ErpFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ERP财务Controller
 */
@RestController
@RequestMapping("/v1/customers/{customerId}")
@RequiredArgsConstructor
@Tag(name = "ERP往来")
public class ErpFinanceController {

    private final ErpFinanceService erpFinanceService;

    @GetMapping("/erp-receivables")
    @Operation(summary = "获取客户应收款")
    public Result<ErpReceivableVO> getReceivables(@PathVariable Long customerId) {
        return Result.ok(erpFinanceService.getReceivables(customerId));
    }

    @GetMapping("/erp-transactions")
    @Operation(summary = "分页获取客户交易记录")
    public Result<PageResult<ErpTransactionVO>> getTransactions(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(erpFinanceService.getTransactions(customerId, pageNum, pageSize));
    }
}
