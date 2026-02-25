package com.aicrm.module.crm.erp.controller;

import com.aicrm.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/open")
@Tag(name = "ERP开放回调接口")
public class OpenWebhookController {

    @PostMapping("/customers/webhook")
    @Operation(summary = "ERP客户变更回调")
    public Result<Void> customerWebhook(@RequestBody(required = false) Map<String, Object> payload) {
        // DEMO: 接收ERP客户变更通知，后续对接实际ERP系统
        return Result.ok();
    }

    @PostMapping("/orders/webhook")
    @Operation(summary = "ERP订单同步回调")
    public Result<Void> orderWebhook(@RequestBody(required = false) Map<String, Object> payload) {
        // DEMO: 接收ERP订单同步通知
        return Result.ok();
    }
}
