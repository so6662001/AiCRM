package com.aicrm.module.crm.report.service;

import com.aicrm.module.crm.report.dto.SalesPersonalReportVO;
import com.aicrm.module.crm.report.dto.TeamActionReportVO;

import java.util.List;

/**
 * 销售报表服务
 */
public interface ReportService {

    SalesPersonalReportVO personalReport(String period);

    List<TeamActionReportVO> teamReport(String period);
}
