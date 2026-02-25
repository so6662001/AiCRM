package com.aicrm.module.crm.opportunity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商机阶段配置DTO
 */
@Data
@Schema(description = "商机阶段配置")
public class StageConfigDTO {

    @NotBlank(message = "阶段名称不能为空")
    @Schema(description = "阶段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stageName;

    @NotBlank(message = "阶段编码不能为空")
    @Schema(description = "阶段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stageCode;

    @Schema(description = "赢单概率")
    private Integer winRate;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否赢单 0否 1是")
    private Integer isWon;

    @Schema(description = "是否输单 0否 1是")
    private Integer isLost;
}
