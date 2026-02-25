package com.aicrm.module.fieldwork.task.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务查询条件")
public class TaskQueryDTO extends PageQuery {

    @Schema(description = "状态 0待开始/1进行中/2已完成/3已取消/4已逾期")
    private Integer status;
    @Schema(description = "执行人用户ID")
    private Long assigneeUserId;
    @Schema(description = "任务类型 1拜访/2电话/3商机/4资料/5其他")
    private Integer taskType;
    @Schema(description = "优先级 1低/2中/3高/4紧急")
    private Integer priority;
    @Schema(description = "计划结束时间早于")
    private LocalDateTime planEndTimeBefore;
}
