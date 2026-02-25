package com.aicrm.module.crm.opportunity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商机阶段配置视图VO
 */
@Data
@Schema(description = "商机阶段配置视图")
public class StageConfigVO {

    private Long id;
    private Long tenantId;
    private String stageName;
    private String stageCode;
    private Integer winRate;
    private Integer sortOrder;
    private Integer isWon;
    private Integer isLost;
    private Integer status;
}
