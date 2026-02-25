package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公开活动报名DTO
 */
@Data
@Schema(description = "公开活动报名请求")
public class OpenRegisterDTO {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "公司名称")
    private String company;
}
