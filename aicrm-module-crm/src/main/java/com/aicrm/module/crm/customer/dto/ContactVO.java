package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人视图对象
 */
@Data
@Schema(description = "联系人详情")
public class ContactVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "性别 0未知/1男/2女")
    private Integer gender;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "部门")
    private String department;
    @Schema(description = "手机")
    private String phone;
    @Schema(description = "座机")
    private String telephone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "微信")
    private String wechat;
    @Schema(description = "是否主联系人 0否/1是")
    private Integer isPrimary;
    @Schema(description = "是否决策人 0否/1是")
    private Integer isDecisionMaker;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
