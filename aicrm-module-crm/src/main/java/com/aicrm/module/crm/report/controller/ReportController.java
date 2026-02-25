package com.aicrm.module.crm.report.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.report.dto.SalesPersonalReportVO;
import com.aicrm.module.crm.report.dto.TeamActionReportVO;
import com.aicrm.module.crm.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售报表Controller
 */
@RestController
@RequestMapping("/v1/reports/sales")
@RequiredArgsConstructor
@Tag(name = "销售报表")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/personal")
    @Operation(summary = "个人销售报表")
    public Result<SalesPersonalReportVO> personalReport(@RequestParam String period) {
        return Result.ok(reportService.personalReport(period));
    }

    @GetMapping("/team")
    @Operation(summary = "团队行动报表")
    public Result<List<TeamActionReportVO>> teamReport(@RequestParam String period) {
        return Result.ok(reportService.teamReport(period));
    }
}
