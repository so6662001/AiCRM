package com.aicrm.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色权限视图VO
 */
@Data
@Schema(description = "角色权限列表")
public class RolePermissionVO {

    @Schema(description = "权限编码列表")
    private List<String> permissionCodes;
}
