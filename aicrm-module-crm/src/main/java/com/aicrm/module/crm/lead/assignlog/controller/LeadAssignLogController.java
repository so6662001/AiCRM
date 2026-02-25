package com.aicrm.module.crm.lead.assignlog.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.lead.assignlog.dto.AssignLogVO;
import com.aicrm.module.crm.lead.assignlog.service.LeadAssignLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线索分配日志控制器
 */
@RestController
@RequestMapping("/v1/leads/{leadId}/assign-logs")
@RequiredArgsConstructor
@Tag(name = "线索分配日志", description = "线索分配日志查询接口")
public class LeadAssignLogController {

    private final LeadAssignLogService leadAssignLogService;

    @GetMapping
    @Operation(summary = "查询线索分配日志列表")
    public Result<List<AssignLogVO>> listByLeadId(@PathVariable Long leadId) {
        return Result.ok(leadAssignLogService.listByLeadId(leadId));
    }
}
