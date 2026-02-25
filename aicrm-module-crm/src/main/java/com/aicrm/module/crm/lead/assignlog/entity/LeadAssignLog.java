package com.aicrm.module.crm.lead.assignlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 线索分配日志实体（轻量日志表，不继承BaseEntity）
 */
@Data
@TableName("lead_assign_log")
public class LeadAssignLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 线索ID */
    private Long leadId;
    /** 分配类型 1手动/2自动/3经理再分配/4退回/5领取 */
    private Integer assignType;
    /** 原负责人用户ID */
    private Long fromUserId;
    /** 新负责人用户ID */
    private Long toUserId;
    /** 分配操作人用户ID */
    private Long assignBy;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
