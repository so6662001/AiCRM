package com.aicrm.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建/更新DTO
 */
@Data
@Schema(description = "角色创建/更新")
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    @Schema(description = "数据范围 1全部 2本部门及以下 3本部门 4仅本人")
    private Integer dataScope;

    @Schema(description = "备注")
    private String remark;
}
