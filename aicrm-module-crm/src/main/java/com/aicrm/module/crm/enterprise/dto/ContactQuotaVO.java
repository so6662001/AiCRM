package com.aicrm.module.crm.enterprise.dto;

import lombok.Data;

/**
 * 联系人配额VO
 */
@Data
public class ContactQuotaVO {

    private Integer dailyQuota;
    private Integer dailyUsed;
    private Integer dailyRemaining;
    private Integer monthlyQuota;
    private Integer monthlyUsed;
    private Integer monthlyRemaining;
}
