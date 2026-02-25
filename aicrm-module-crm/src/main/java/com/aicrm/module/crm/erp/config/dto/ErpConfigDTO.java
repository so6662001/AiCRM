package com.aicrm.module.crm.erp.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ERP配置DTO
 */
@Data
@Schema(description = "ERP配置")
public class ErpConfigDTO {

    @NotBlank(message = "ERP名称不能为空")
    @Schema(description = "ERP名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String erpName;

    @NotBlank(message = "API基础URL不能为空")
    @Schema(description = "API基础URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiBaseUrl;

    @Schema(description = "ERP类型")
    private String erpType;

    @Schema(description = "认证类型")
    private String authType;

    @Schema(description = "认证配置JSON")
    private String authConfig;

    @Schema(description = "同步策略")
    private String syncStrategy;

    @Schema(description = "同步Cron表达式")
    private String syncCron;
}
