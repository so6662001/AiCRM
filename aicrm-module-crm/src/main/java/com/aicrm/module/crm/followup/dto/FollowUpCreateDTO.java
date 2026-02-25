package com.aicrm.module.crm.followup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建跟进记录DTO
 */
@Data
@Schema(description = "创建跟进记录请求")
public class FollowUpCreateDTO {

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型 1线索/2客户/3商机", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer bizType;

    @NotNull(message = "业务ID不能为空")
    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bizId;

    @Schema(description = "客户ID")
    private Long customerId;

    @NotNull(message = "跟进方式不能为空")
    @Schema(description = "跟进方式 1现场拜访/2电话/3微信/4邮件/5企业微信/6其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer followType;

    @NotBlank(message = "跟进内容不能为空")
    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "下次跟进时间")
    private LocalDateTime nextFollowTime;

    @Schema(description = "下次跟进备注")
    private String nextFollowNote;
}
