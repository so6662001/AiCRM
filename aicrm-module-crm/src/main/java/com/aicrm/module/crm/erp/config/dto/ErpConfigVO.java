package com.aicrm.module.crm.erp.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP配置视图VO（authConfig脱敏）
 */
@Data
@Schema(description = "ERP配置视图")
public class ErpConfigVO {

    private Long id;
    private Long tenantId;
    private String erpType;
    private String erpName;
    private String apiBaseUrl;
    private String authType;
    /** 脱敏后的认证配置 */
    private String authConfig;
    private String fieldMapping;
    private String syncStrategy;
    private String syncCron;
    private Integer status;
    private LocalDateTime lastSyncTime;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
}
