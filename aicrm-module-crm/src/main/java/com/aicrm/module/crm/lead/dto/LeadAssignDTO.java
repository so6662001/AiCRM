package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 线索分配DTO
 */
@Data
@Schema(description = "线索分配请求")
public class LeadAssignDTO {

    @NotEmpty(message = "线索ID列表不能为空")
    @Schema(description = "线索ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> leadIds;

    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetUserId;
}
