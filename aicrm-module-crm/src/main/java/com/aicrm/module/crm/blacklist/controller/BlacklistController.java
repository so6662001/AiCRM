package com.aicrm.module.crm.blacklist.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.blacklist.dto.BlacklistCreateDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistQueryDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistVO;
import com.aicrm.module.crm.blacklist.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单Controller
 */
@RestController
@RequestMapping("/v1/customers/blacklist")
@RequiredArgsConstructor
@Tag(name = "客户黑名单")
public class BlacklistController {

    private final BlacklistService blacklistService;

    @GetMapping
    @Operation(summary = "分页查询黑名单")
    public Result<PageResult<BlacklistVO>> page(@Valid BlacklistQueryDTO query) {
        return Result.ok(blacklistService.page(query));
    }

    @PostMapping
    @Operation(summary = "添加黑名单")
    public Result<Long> addToBlacklist(@Valid @RequestBody BlacklistCreateDTO dto) {
        return Result.ok(blacklistService.addToBlacklist(dto));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "解除黑名单")
    public Result<Void> release(@PathVariable Long id, @RequestBody ReleaseRequest request) {
        blacklistService.release(id, request != null ? request.getReleaseReason() : null);
        return Result.ok();
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否在黑名单")
    public Result<Boolean> isBlacklisted(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String phone) {
        return Result.ok(blacklistService.isBlacklisted(companyName, phone));
    }

    @Data
    public static class ReleaseRequest {
        private String releaseReason;
    }
}
