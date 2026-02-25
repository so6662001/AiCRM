package com.aicrm.module.crm.enterprise.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.enterprise.dto.ContactImportDTO;
import com.aicrm.module.crm.enterprise.dto.ContactQuotaVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseContactVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseSearchVO;
import com.aicrm.module.crm.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 五度易链企业信息查询Controller
 */
@RestController
@RequestMapping("/v1/enterprise")
@RequiredArgsConstructor
@Tag(name = "企业信息查询")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @GetMapping("/search")
    @Operation(summary = "搜索企业")
    public Result<List<EnterpriseSearchVO>> search(@RequestParam String keyword) {
        return Result.ok(enterpriseService.search(keyword));
    }

    @GetMapping("/{creditCode}/contacts")
    @Operation(summary = "查询企业联系人")
    public Result<List<EnterpriseContactVO>> queryContacts(@PathVariable String creditCode) {
        return Result.ok(enterpriseService.queryContacts(creditCode));
    }

    @GetMapping("/{creditCode}/contacts/cache")
    @Operation(summary = "获取企业联系人缓存")
    public Result<List<EnterpriseContactVO>> getContactsCache(@PathVariable String creditCode) {
        return Result.ok(enterpriseService.getContactsCache(creditCode));
    }

    @PostMapping("/{creditCode}/contacts/{contactId}/import")
    @Operation(summary = "导入联系人")
    public Result<Void> importContact(
            @PathVariable String creditCode,
            @PathVariable Long contactId,
            @Valid @RequestBody ContactImportDTO dto) {
        enterpriseService.importContact(creditCode, contactId, dto);
        return Result.ok();
    }

    @GetMapping("/contact-quota")
    @Operation(summary = "获取联系人配额")
    public Result<ContactQuotaVO> getContactQuota() {
        return Result.ok(enterpriseService.getContactQuota());
    }
}
