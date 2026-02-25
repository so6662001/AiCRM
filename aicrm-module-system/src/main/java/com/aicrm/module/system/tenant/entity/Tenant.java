package com.aicrm.module.system.tenant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户实体（平台级表，无 tenant_id）
 */
@Data
@TableName("tenant")
public class Tenant implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 租户编码 */
    private String tenantCode;
    /** 租户名称 */
    private String tenantName;
    /** Logo URL */
    private String logoUrl;
    /** 联系人姓名 */
    private String contactName;
    /** 联系电话 */
    private String contactPhone;
    /** 联系邮箱 */
    private String contactEmail;
    /** 状态 0禁用/1启用/2试用 */
    private Integer status;
    /** 版本 basic/pro/enterprise */
    private String edition;
    /** 最大用户数 */
    private Integer maxUsers;
    /** 到期日期 */
    private LocalDate expireDate;
    /** 存储配额(MB) */
    private Long storageQuotaMb;
    /** 已用存储(MB) */
    private Long storageUsedMb;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
