package com.aicrm.module.system.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户视图对象
 */
@Data
@Schema(description = "租户详情")
public class TenantVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户编码")
    private String tenantCode;
    @Schema(description = "租户名称")
    private String tenantName;
    @Schema(description = "Logo URL")
    private String logoUrl;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "联系邮箱")
    private String contactEmail;
    @Schema(description = "状态 0禁用/1启用/2试用")
    private Integer status;
    @Schema(description = "版本 basic/pro/enterprise")
    private String edition;
    @Schema(description = "最大用户数")
    private Integer maxUsers;
    @Schema(description = "到期日期")
    private LocalDate expireDate;
    @Schema(description = "存储配额(MB)")
    private Long storageQuotaMb;
    @Schema(description = "已用存储(MB)")
    private Long storageUsedMb;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
