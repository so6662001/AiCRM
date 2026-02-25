package com.aicrm.module.crm.opportunity.stagelog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商机阶段变更日志视图VO
 */
@Data
@Schema(description = "商机阶段变更日志视图")
public class StageLogVO {

    private Long id;
    private Long tenantId;
    private Long opportunityId;
    private Long fromStageId;
    private Long toStageId;
    private Integer stayDays;
    private String remark;
    private Long operatedBy;
    private LocalDateTime createdTime;

    @Schema(description = "原阶段名称")
    private String fromStageName;
    @Schema(description = "新阶段名称")
    private String toStageName;
    @Schema(description = "操作人姓名")
    private String operatedByName;
}
