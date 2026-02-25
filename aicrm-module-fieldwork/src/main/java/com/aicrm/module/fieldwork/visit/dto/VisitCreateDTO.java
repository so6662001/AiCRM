package com.aicrm.module.fieldwork.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建拜访DTO
 */
@Data
@Schema(description = "创建拜访请求")
public class VisitCreateDTO {

    @NotNull(message = "客户ID不能为空")
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @NotNull(message = "拜访类型不能为空")
    @Schema(description = "拜访类型 1现场/2电话/3微信/4企微/5视频", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer visitType;

    @Schema(description = "拜访目的")
    private String visitPurpose;

    @NotNull(message = "拜访时间不能为空")
    @Schema(description = "拜访时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime visitTime;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "通话号码")
    private String callPhone;

    @Schema(description = "备注")
    private String remark;
}
