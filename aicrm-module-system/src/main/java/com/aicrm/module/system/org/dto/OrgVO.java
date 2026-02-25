package com.aicrm.module.system.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织视图对象（含树结构 children）
 */
@Data
@Schema(description = "组织详情")
public class OrgVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "父级ID")
    private Long parentId;
    @Schema(description = "组织名称")
    private String orgName;
    @Schema(description = "组织编码")
    private String orgCode;
    @Schema(description = "组织类型 1公司/2部门/3团队")
    private Integer orgType;
    @Schema(description = "组织路径")
    private String orgPath;
    @Schema(description = "负责人用户ID")
    private Long leaderUserId;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "子组织列表")
    private List<OrgVO> children;
}
