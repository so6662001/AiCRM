package com.aicrm.module.crm.opportunity.stagelog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商机阶段变更日志实体（轻量日志表，不继承BaseEntity）
 */
@Data
@TableName("opportunity_stage_log")
public class OpportunityStageLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 商机ID */
    private Long opportunityId;
    /** 原阶段ID */
    private Long fromStageId;
    /** 新阶段ID */
    private Long toStageId;
    /** 在原阶段停留天数 */
    private Integer stayDays;
    /** 备注 */
    private String remark;
    /** 操作人用户ID */
    private Long operatedBy;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
