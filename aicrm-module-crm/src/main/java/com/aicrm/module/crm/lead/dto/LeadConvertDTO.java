package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 线索转化DTO
 */
@Data
@Schema(description = "线索转化请求")
public class LeadConvertDTO {

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "是否创建商机")
    private Boolean createOpportunity;
}
