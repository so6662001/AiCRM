package com.aicrm.module.crm.followup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新跟进记录DTO
 */
@Data
@Schema(description = "更新跟进记录请求")
public class FollowUpUpdateDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "跟进记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "跟进内容")
    private String content;

    @Schema(description = "下次跟进时间")
    private LocalDateTime nextFollowTime;

    @Schema(description = "下次跟进备注")
    private String nextFollowNote;
}
