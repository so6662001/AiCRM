package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 线索退回公海池DTO
 */
@Data
@Schema(description = "线索退回公海池请求")
public class LeadReturnDTO {

    @Schema(description = "退回原因")
    private String returnReason;
}
