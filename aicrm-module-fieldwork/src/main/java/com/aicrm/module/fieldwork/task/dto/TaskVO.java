package com.aicrm.module.fieldwork.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务VO
 */
@Data
@Schema(description = "任务视图")
public class TaskVO {

    private Long id;
    private Long tenantId;
    private String taskNo;
    private String taskTitle;
    private String taskContent;
    private Integer taskType;
    private Integer priority;
    private Integer status;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String completionNote;
    private Integer completionRate;
    private Integer assignType;
    private Long assigneeUserId;
    private String assigneeUserName;
    private Long assignerUserId;
    private String assignerUserName;
    private Integer relatedBizType;
    private Long relatedBizId;
    private Long relatedVisitId;
    private LocalDateTime remindTime;
    private Integer isReminded;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
