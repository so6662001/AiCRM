package com.aicrm.module.crm.lead.pool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公海池配置视图VO
 */
@Data
@Schema(description = "公海池配置视图")
public class PoolConfigVO {

    private Long id;
    private Long tenantId;
    private String poolName;
    private Integer recycleDays;
    private Integer maxHoldCount;
    private Integer dailyPickLimit;
    private String visibleOrgIds;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
}
