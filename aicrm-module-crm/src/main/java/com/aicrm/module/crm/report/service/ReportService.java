package com.aicrm.module.crm.report.service;

import com.aicrm.module.crm.report.dto.SalesPersonalReportVO;
import com.aicrm.module.crm.report.dto.TeamActionReportVO;

import java.util.List;
import java.util.Map;

/**
 * 销售报表服务
 */
public interface ReportService {

    SalesPersonalReportVO personalReport(String period);

    List<TeamActionReportVO> teamReport(String period);

    Map<String, Object> dashboard();

    Map<String, Object> leadConversion();

    Map<String, Object> followUpStatistics();

    Map<String, Object> visitStatistics();

    Map<String, Object> violationStatistics();

    List<Map<String, Object>> ranking();

    Map<String, Object> salesPerformance();
}
