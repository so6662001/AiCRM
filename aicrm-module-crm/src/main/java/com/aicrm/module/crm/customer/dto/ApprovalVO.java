package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户审核视图对象
 */
@Data
@Schema(description = "客户审核详情")
public class ApprovalVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "审核类型 1新增/2ERP下发")
    private Integer approvalType;
    @Schema(description = "状态 1待审核/2通过/3拒绝")
    private Integer status;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "审核人ID")
    private Long approverId;
    @Schema(description = "审核时间")
    private LocalDateTime approveTime;
    @Schema(description = "审核备注")
    private String approveRemark;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
