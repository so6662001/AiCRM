package com.aicrm.module.crm.violation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 违规词DTO
 */
@Data
@Schema(description = "违规词")
public class ViolationWordDTO {

    @NotBlank(message = "违规词不能为空")
    @Schema(description = "违规词", requiredMode = Schema.RequiredMode.REQUIRED)
    private String word;

    @Schema(description = "分类 虚假承诺/过度宣传/恶意攻击/其他")
    private String category;

    @NotNull(message = "等级不能为空")
    @Schema(description = "等级 1低/2中/3高", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;
}
