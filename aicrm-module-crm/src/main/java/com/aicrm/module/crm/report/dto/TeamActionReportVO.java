package com.aicrm.module.crm.report.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 团队行动报表VO
 */
@Data
public class TeamActionReportVO implements Serializable {

    private Long userId;
    private String userName;
    private Integer followUps;
    private Integer visits;
    private Integer callDuration;
    private Integer newCustomers;
    private BigDecimal dealAmount;
    private Integer overdueLeads;
}
