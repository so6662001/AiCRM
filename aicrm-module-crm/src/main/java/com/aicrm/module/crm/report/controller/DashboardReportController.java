package com.aicrm.module.crm.report.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表仪表盘控制器
 */
@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Tag(name = "报表仪表盘")
public class DashboardReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘数据")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(reportService.dashboard());
    }

    @GetMapping("/lead-conversion")
    @Operation(summary = "线索转化率")
    public Result<Map<String, Object>> leadConversion() {
        return Result.ok(reportService.leadConversion());
    }

    @GetMapping("/follow-up-statistics")
    @Operation(summary = "跟进统计")
    public Result<Map<String, Object>> followUpStatistics() {
        return Result.ok(reportService.followUpStatistics());
    }

    @GetMapping("/visit-statistics")
    @Operation(summary = "拜访统计")
    public Result<Map<String, Object>> visitStatistics() {
        return Result.ok(reportService.visitStatistics());
    }

    @GetMapping("/violation-statistics")
    @Operation(summary = "违规统计")
    public Result<Map<String, Object>> violationStatistics() {
        return Result.ok(reportService.violationStatistics());
    }

    @GetMapping("/ranking")
    @Operation(summary = "销售排行")
    public Result<List<Map<String, Object>>> ranking() {
        return Result.ok(reportService.ranking());
    }

    @GetMapping("/sales-performance")
    @Operation(summary = "销售业绩")
    public Result<Map<String, Object>> salesPerformance() {
        return Result.ok(reportService.salesPerformance());
    }
}
