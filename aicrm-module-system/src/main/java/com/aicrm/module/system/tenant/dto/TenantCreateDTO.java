package com.aicrm.module.system.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 租户创建/更新 DTO
 */
@Data
@Schema(description = "租户创建/更新请求")
public class TenantCreateDTO {

    @NotBlank(message = "租户编码不能为空")
    @Schema(description = "租户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantCode;

    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "版本 basic/pro/enterprise")
    private String edition;

    @Schema(description = "最大用户数")
    private Integer maxUsers;

    @Schema(description = "到期日期")
    private LocalDate expireDate;
}
