package com.aicrm.module.crm.opportunity.stagelog.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.opportunity.stagelog.dto.StageLogVO;
import com.aicrm.module.crm.opportunity.stagelog.service.OpportunityStageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商机阶段变更日志控制器
 */
@RestController
@RequestMapping("/v1/opportunities/{opportunityId}/stage-logs")
@RequiredArgsConstructor
@Tag(name = "商机阶段变更日志", description = "商机阶段变更日志查询接口")
public class OpportunityStageLogController {

    private final OpportunityStageLogService opportunityStageLogService;

    @GetMapping
    @Operation(summary = "查询商机阶段变更日志列表")
    public Result<List<StageLogVO>> listByOpportunityId(@PathVariable Long opportunityId) {
        return Result.ok(opportunityStageLogService.listByOpportunityId(opportunityId));
    }
}
