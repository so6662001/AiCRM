package com.aicrm.module.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新好友DTO
 */
@Data
@Schema(description = "更新好友请求")
public class FriendUpdateDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "好友ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "好友姓名")
    private String friendName;

    @Schema(description = "好友公司")
    private String friendCompany;

    @Schema(description = "好友职位")
    private String friendPosition;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "备注")
    private String remark;
}
