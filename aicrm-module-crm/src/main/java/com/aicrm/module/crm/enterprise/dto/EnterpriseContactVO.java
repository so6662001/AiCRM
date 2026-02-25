package com.aicrm.module.crm.enterprise.dto;

import lombok.Data;

/**
 * 企业联系人VO
 */
@Data
public class EnterpriseContactVO {

    private Long id;
    private String contactName;
    private String position;
    private String department;
    /** 手机号脱敏 */
    private String phone;
    /** 完整手机号 */
    private String phoneFull;
    private String telephone;
    /** 邮箱脱敏 */
    private String email;
    /** 完整邮箱 */
    private String emailFull;
    private String sourceType;
    private String sourceTypeLabel;
    private Integer reliability;
    private String reliabilityLabel;
    /** 是否已导入 */
    private Boolean isImported;
    private Long importedCustomerId;
    /** 是否存在于CRM */
    private Boolean existsInCrm;
}
