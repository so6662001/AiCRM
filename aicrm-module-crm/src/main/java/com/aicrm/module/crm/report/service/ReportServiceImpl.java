package com.aicrm.module.crm.report.service;

import com.aicrm.module.crm.report.dto.SalesPersonalReportVO;
import com.aicrm.module.crm.report.dto.TeamActionReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售报表服务实现 - DEMO版返回模拟数据
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Override
    public SalesPersonalReportVO personalReport(String period) {
        SalesPersonalReportVO vo = new SalesPersonalReportVO();
        vo.setPeriod(period);
        vo.setDealAmount(new BigDecimal("850000"));
        vo.setDealTarget(new BigDecimal("1000000"));
        vo.setCompletionRate(85.0);
        vo.setDealCount(5);
        vo.setNewCustomers(12);
        vo.setTotalFollowUps(68);
        vo.setDailyAvgFollowUps(3.4);
        vo.setTotalVisits(15);
        vo.setOnsiteVisits(10);
        vo.setPhoneVisits(5);
        vo.setTotalCallDuration(3600);
        vo.setActiveOpportunities(8);
        vo.setWonCount(3);
        vo.setWonAmount(new BigDecimal("520000"));
        vo.setLostCount(1);
        vo.setViolationCount(0);
        return vo;
    }

    @Override
    public List<TeamActionReportVO> teamReport(String period) {
        TeamActionReportVO vo1 = new TeamActionReportVO();
        vo1.setUserId(1001L);
        vo1.setUserName("张三");
        vo1.setFollowUps(25);
        vo1.setVisits(8);
        vo1.setCallDuration(1200);
        vo1.setNewCustomers(10);
        vo1.setDealAmount(new BigDecimal("850000"));
        vo1.setOverdueLeads(2);

        TeamActionReportVO vo2 = new TeamActionReportVO();
        vo2.setUserId(1002L);
        vo2.setUserName("李四");
        vo2.setFollowUps(18);
        vo2.setVisits(5);
        vo2.setCallDuration(900);
        vo2.setNewCustomers(6);
        vo2.setDealAmount(new BigDecimal("420000"));
        vo2.setOverdueLeads(1);

        TeamActionReportVO vo3 = new TeamActionReportVO();
        vo3.setUserId(1003L);
        vo3.setUserName("王五");
        vo3.setFollowUps(25);
        vo3.setVisits(2);
        vo3.setCallDuration(1500);
        vo3.setNewCustomers(8);
        vo3.setDealAmount(new BigDecimal("380000"));
        vo3.setOverdueLeads(3);

        return List.of(vo1, vo2, vo3);
    }

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", Map.of(
                "totalLeads", 156,
                "totalCustomers", 89,
                "totalOpportunities", 23,
                "totalDealAmount", new BigDecimal("1250000")
        ));
        result.put("leadConversion", Map.of(
                "conversionRate", 32.5,
                "todayConverted", 5,
                "monthConverted", 28
        ));
        result.put("topSales", List.of(
                Map.of("userId", 1001, "userName", "张三", "dealAmount", new BigDecimal("520000")),
                Map.of("userId", 1002, "userName", "李四", "dealAmount", new BigDecimal("380000")),
                Map.of("userId", 1003, "userName", "王五", "dealAmount", new BigDecimal("350000"))
        ));
        result.put("todayActivities", Map.of(
                "followUps", 12,
                "visits", 5,
                "newLeads", 8
        ));
        return result;
    }

    @Override
    public Map<String, Object> leadConversion() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLeads", 156);
        result.put("convertedLeads", 51);
        result.put("conversionRate", 32.7);
        result.put("bySource", List.of(
                Map.of("source", "manual", "count", 45, "converted", 18),
                Map.of("source", "website", "count", 62, "converted", 22),
                Map.of("source", "referral", "count", 49, "converted", 11)
        ));
        return result;
    }

    @Override
    public Map<String, Object> followUpStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", 368);
        result.put("todayCount", 25);
        result.put("byType", List.of(
                Map.of("type", "现场", "count", 85),
                Map.of("type", "电话", "count", 120),
                Map.of("type", "微信", "count", 98),
                Map.of("type", "企微", "count", 65)
        ));
        return result;
    }

    @Override
    public Map<String, Object> visitStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalVisits", 156);
        result.put("completedVisits", 142);
        result.put("onSiteVisits", 98);
        result.put("phoneVisits", 44);
        result.put("avgDuration", 45);
        return result;
    }

    @Override
    public Map<String, Object> violationStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalViolations", 3);
        result.put("thisMonth", 1);
        result.put("byLevel", List.of(
                Map.of("level", "高", "count", 0),
                Map.of("level", "中", "count", 1),
                Map.of("level", "低", "count", 2)
        ));
        return result;
    }

    @Override
    public List<Map<String, Object>> ranking() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("rank", 1, "userId", 1001, "userName", "张三", "dealAmount", new BigDecimal("520000"), "dealCount", 5));
        list.add(Map.of("rank", 2, "userId", 1002, "userName", "李四", "dealAmount", new BigDecimal("380000"), "dealCount", 3));
        list.add(Map.of("rank", 3, "userId", 1003, "userName", "王五", "dealAmount", new BigDecimal("350000"), "dealCount", 4));
        list.add(Map.of("rank", 4, "userId", 1004, "userName", "赵六", "dealAmount", new BigDecimal("280000"), "dealCount", 2));
        list.add(Map.of("rank", 5, "userId", 1005, "userName", "钱七", "dealAmount", new BigDecimal("210000"), "dealCount", 2));
        return list;
    }

    @Override
    public Map<String, Object> salesPerformance() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", "2025-02");
        result.put("totalDealAmount", new BigDecimal("1740000"));
        result.put("targetAmount", new BigDecimal("2000000"));
        result.put("completionRate", 87.0);
        result.put("dealCount", 16);
        result.put("newCustomers", 42);
        return result;
    }
}
