package com.aicrm.module.crm.erp.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP同步明细视图VO
 */
@Data
@Schema(description = "ERP同步明细视图")
public class SyncDetailVO {

    private Long id;
    private Long tenantId;
    private Long syncLogId;
    private Long crmBizId;
    private String erpBizId;
    private Integer syncAction;
    private Integer status;
    private String requestData;
    private String responseData;
    private String errorMessage;
    private LocalDateTime createdTime;
}
