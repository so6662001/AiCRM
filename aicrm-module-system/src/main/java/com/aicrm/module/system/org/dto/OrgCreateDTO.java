package com.aicrm.module.system.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 组织创建/更新 DTO
 */
@Data
@Schema(description = "组织创建/更新请求")
public class OrgCreateDTO {

    @NotBlank(message = "组织名称不能为空")
    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgName;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "组织编码")
    private String orgCode;

    @Schema(description = "组织类型 1公司/2部门/3团队")
    private Integer orgType;

    @Schema(description = "负责人用户ID")
    private Long leaderUserId;

    @Schema(description = "排序")
    private Integer sortOrder;
}
