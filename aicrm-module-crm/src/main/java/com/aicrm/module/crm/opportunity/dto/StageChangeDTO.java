package com.aicrm.module.crm.opportunity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商机阶段变更DTO
 */
@Data
@Schema(description = "商机阶段变更请求")
public class StageChangeDTO {

    @NotNull(message = "阶段ID不能为空")
    @Schema(description = "阶段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long stageId;

    @Schema(description = "备注")
    private String remark;
}
