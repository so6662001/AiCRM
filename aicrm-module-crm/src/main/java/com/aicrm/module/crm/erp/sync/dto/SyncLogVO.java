package com.aicrm.module.crm.erp.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP同步日志视图VO
 */
@Data
@Schema(description = "ERP同步日志视图")
public class SyncLogVO {

    private Long id;
    private Long tenantId;
    private Long erpConfigId;
    private Integer syncType;
    private Integer syncMode;
    private String bizType;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Integer skipCount;
    private Integer status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long operatedBy;
    private LocalDateTime createdTime;

    @Schema(description = "同步类型标签")
    private String syncTypeLabel;
    @Schema(description = "同步模式标签")
    private String syncModeLabel;
    @Schema(description = "状态标签")
    private String statusLabel;
}
