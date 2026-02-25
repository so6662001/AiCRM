package com.aicrm.module.crm.opportunity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建商机DTO
 */
@Data
@Schema(description = "创建商机请求")
public class OpportunityCreateDTO {

    @NotBlank(message = "商机名称不能为空")
    @Schema(description = "商机名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String opportunityName;

    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "阶段ID")
    private Long stageId;

    @Schema(description = "预计金额")
    private BigDecimal expectedAmount;

    @Schema(description = "预计成交日期")
    private LocalDate expectedCloseDate;

    @Schema(description = "竞争对手")
    private String competitor;

    @Schema(description = "备注")
    private String remark;
}
