package com.aicrm.module.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户审核实体（轻量级，不继承BaseEntity）
 */
@Data
@TableName("customer_approval")
public class CustomerApproval {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 客户ID */
    private Long customerId;
    /** 审核类型 1新增/2ERP下发 */
    private Integer approvalType;
    /** 状态 1待审核/2通过/3拒绝 */
    private Integer status;
    /** 申请人ID */
    private Long applicantId;
    /** 审核人ID */
    private Long approverId;
    /** 审核时间 */
    private LocalDateTime approveTime;
    /** 审核备注 */
    private String approveRemark;
    /** 创建时间 */
    private LocalDateTime createdTime;

    @TableLogic
    private Integer deleted;
}
