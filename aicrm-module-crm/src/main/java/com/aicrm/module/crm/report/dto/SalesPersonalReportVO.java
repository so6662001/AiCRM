package com.aicrm.module.crm.report.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售员个人报表VO
 */
@Data
public class SalesPersonalReportVO implements Serializable {

    private String period;
    private BigDecimal dealAmount;
    private BigDecimal dealTarget;
    private Double completionRate;
    private Integer dealCount;
    private Integer newCustomers;
    private Integer totalFollowUps;
    private Double dailyAvgFollowUps;
    private Integer totalVisits;
    private Integer onsiteVisits;
    private Integer phoneVisits;
    private Integer totalCallDuration;
    private Integer activeOpportunities;
    private Integer wonCount;
    private BigDecimal wonAmount;
    private Integer lostCount;
    private Integer violationCount;
}
