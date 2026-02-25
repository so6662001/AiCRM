package com.aicrm.module.crm.lead.pool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公海池配置DTO
 */
@Data
@Schema(description = "公海池配置")
public class PoolConfigDTO {

    @NotBlank(message = "公海池名称不能为空")
    @Schema(description = "公海池名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolName;

    @Schema(description = "回收天数")
    private Integer recycleDays;

    @Schema(description = "最大持有数量")
    private Integer maxHoldCount;

    @Schema(description = "每日领取上限")
    private Integer dailyPickLimit;

    @Schema(description = "可见组织ID列表，逗号分隔")
    private String visibleOrgIds;
}
