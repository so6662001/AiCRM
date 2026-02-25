package com.aicrm.module.crm.enterprise.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 企业搜索VO
 */
@Data
public class EnterpriseSearchVO {

    private String companyName;
    private String creditCode;
    private String legalPerson;
    private String registeredCapital;
    private LocalDate establishedDate;
    private String companyStatus;
    private String industry;
    private String province;
    private String city;
    /** 是否平台客户 */
    private Boolean isPlatformCustomer;
    private Long matchedCustomerId;
    private String matchedCustomerName;
    /** 已缓存联系人数 */
    private Integer contactCacheCount;
}
