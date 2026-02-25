package com.aicrm.module.fieldwork.task.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_task")
public class WorkTask extends BaseEntity {

    /** 任务编号 */
    private String taskNo;
    /** 任务标题 */
    private String taskTitle;
    /** 任务内容 */
    private String taskContent;
    /** 任务类型 1拜访/2电话/3商机/4资料/5其他 */
    private Integer taskType;
    /** 优先级 1低/2中/3高/4紧急 */
    private Integer priority;
    /** 状态 0待开始/1进行中/2已完成/3已取消/4已逾期 */
    private Integer status;
    /** 计划开始时间 */
    private LocalDateTime planStartTime;
    /** 计划结束时间 */
    private LocalDateTime planEndTime;
    /** 实际开始时间 */
    private LocalDateTime actualStartTime;
    /** 实际结束时间 */
    private LocalDateTime actualEndTime;
    /** 完成备注 */
    private String completionNote;
    /** 完成率 */
    private Integer completionRate;
    /** 分配方式 1自主/2上级安排 */
    private Integer assignType;
    /** 执行人用户ID */
    private Long assigneeUserId;
    /** 分配人用户ID */
    private Long assignerUserId;
    /** 关联业务类型 */
    private Integer relatedBizType;
    /** 关联业务ID */
    private Long relatedBizId;
    /** 关联拜访ID */
    private Long relatedVisitId;
    /** 提醒时间 */
    private LocalDateTime remindTime;
    /** 是否已提醒 0否/1是 */
    private Integer isReminded;
}
