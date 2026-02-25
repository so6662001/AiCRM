package com.aicrm.module.crm.report.service;

import com.aicrm.module.crm.report.dto.SalesPersonalReportVO;
import com.aicrm.module.crm.report.dto.TeamActionReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
}
