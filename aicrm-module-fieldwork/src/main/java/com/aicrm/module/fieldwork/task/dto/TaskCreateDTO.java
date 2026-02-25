package com.aicrm.module.fieldwork.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建任务DTO
 */
@Data
@Schema(description = "创建任务请求")
public class TaskCreateDTO {

    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskTitle;

    @Schema(description = "任务内容")
    private String taskContent;

    @NotNull(message = "任务类型不能为空")
    @Schema(description = "任务类型 1拜访/2电话/3商机/4资料/5其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer taskType;

    @Schema(description = "优先级 1低/2中/3高/4紧急")
    private Integer priority;

    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "分配方式 1自主/2上级安排")
    private Integer assignType;

    @NotNull(message = "执行人不能为空")
    @Schema(description = "执行人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long assigneeUserId;

    @Schema(description = "分配人用户ID")
    private Long assignerUserId;

    @Schema(description = "关联业务类型")
    private Integer relatedBizType;

    @Schema(description = "关联业务ID")
    private Long relatedBizId;

    @Schema(description = "提醒时间")
    private LocalDateTime remindTime;
}
