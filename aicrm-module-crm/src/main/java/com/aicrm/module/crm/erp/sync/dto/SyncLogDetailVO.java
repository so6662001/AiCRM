package com.aicrm.module.crm.erp.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * ERP同步日志详情VO（含明细列表）
 */
@Data
@Schema(description = "ERP同步日志详情")
public class SyncLogDetailVO extends SyncLogVO {

    @Schema(description = "同步明细列表")
    private List<SyncDetailVO> details;
}
