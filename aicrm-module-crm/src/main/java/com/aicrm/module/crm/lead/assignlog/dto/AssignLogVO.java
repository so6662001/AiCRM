package com.aicrm.module.crm.lead.assignlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线索分配日志视图VO
 */
@Data
@Schema(description = "线索分配日志视图")
public class AssignLogVO {

    private Long id;
    private Long tenantId;
    private Long leadId;
    private Integer assignType;
    private Long fromUserId;
    private Long toUserId;
    private Long assignBy;
    private String remark;
    private LocalDateTime createdTime;

    @Schema(description = "原负责人姓名")
    private String fromUserName;
    @Schema(description = "新负责人姓名")
    private String toUserName;
    @Schema(description = "分配操作人姓名")
    private String assignByName;
}
