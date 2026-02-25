package com.aicrm.module.crm.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业查询缓存实体（轻量级缓存表）
 */
@Data
@TableName("enterprise_query_cache")
public class EnterpriseQueryCache {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String companyName;
    private String creditCode;
    private String legalPerson;
    private String registeredCapital;
    private LocalDate establishedDate;
    private String companyStatus;
    private String companyType;
    private String industry;
    private String province;
    private String city;
    private String address;
    private String businessScope;
    private String contactPhone;
    private String contactEmail;
    private String website;
    /** 来源，默认wdyl */
    private String source = "wdyl";
    /** 是否平台客户 0否/1是 */
    private Integer isPlatformCustomer;
    private Long matchedCustomerId;
    private LocalDateTime queryTime;
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
}
