package com.aicrm.module.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建好友DTO
 */
@Data
@Schema(description = "创建好友请求")
public class FriendCreateDTO {

    @NotBlank(message = "好友姓名不能为空")
    @Schema(description = "好友姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String friendName;

    @NotBlank(message = "好友手机号不能为空")
    @Schema(description = "好友手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String friendPhone;

    @NotBlank(message = "好友公司不能为空")
    @Schema(description = "好友公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String friendCompany;

    @NotBlank(message = "好友职位不能为空")
    @Schema(description = "好友职位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String friendPosition;

    @NotNull(message = "好友类型不能为空")
    @Schema(description = "好友类型 1平台/2企微/3双渠道", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer friendType;

    @NotNull(message = "来源不能为空")
    @Schema(description = "来源 1活动扫码/2企微同步/3手动添加/4线索导入/5名片扫描", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer source;

    @Schema(description = "来源活动ID")
    private Long sourceActivityId;

    @Schema(description = "备注")
    private String remark;
}
