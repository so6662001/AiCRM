package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建参与人DTO
 */
@Data
@Schema(description = "创建参与人请求")
public class ParticipantCreateDTO {

    @NotBlank(message = "参与人姓名不能为空")
    @Schema(description = "参与人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String participantName;

    @Schema(description = "参与人电话")
    private String participantPhone;

    @Schema(description = "参与人邮箱")
    private String participantEmail;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位")
    private String position;

    @NotNull(message = "来源不能为空")
    @Schema(description = "来源 1扫码报名/2手动录入/3批量导入/4扫码参与", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer source;
}
