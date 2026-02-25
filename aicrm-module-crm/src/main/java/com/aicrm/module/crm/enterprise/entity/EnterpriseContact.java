package com.aicrm.module.crm.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业联系人实体
 */
@Data
@TableName("enterprise_contact")
public class EnterpriseContact {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String creditCode;
    private String companyName;
    private String contactName;
    private String position;
    private String department;
    private String phone;
    private String telephone;
    private String email;
    /** 来源，默认wdyl */
    private String source = "wdyl";
    private String sourceType;
    /** 可信度 1低/2中/3高 */
    private Integer reliability;
    /** 是否已导入 0否/1是 */
    private Integer isImported;
    private Long importedCustomerId;
    private Long importedContactId;
    private Long queryUserId;
    private LocalDateTime queryTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    @TableLogic
    private Integer deleted = 0;
}
