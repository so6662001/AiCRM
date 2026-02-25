package com.aicrm.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色视图VO
 */
@Data
@Schema(description = "角色视图")
public class RoleVO {

    private Long id;
    private Long tenantId;
    private String roleCode;
    private String roleName;
    private Integer roleType;
    private Integer dataScope;
    private String remark;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
}
