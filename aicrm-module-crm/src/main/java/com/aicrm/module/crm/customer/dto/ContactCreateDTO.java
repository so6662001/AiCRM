package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 联系人创建/更新DTO
 */
@Data
@Schema(description = "联系人创建/更新请求")
public class ContactCreateDTO {

    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactName;

    @Schema(description = "手机")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "性别 0未知/1男/2女")
    private Integer gender;

    @Schema(description = "是否主联系人 0否/1是")
    private Integer isPrimary;

    @Schema(description = "是否决策人 0否/1是")
    private Integer isDecisionMaker;

    @Schema(description = "备注")
    private String remark;
}
