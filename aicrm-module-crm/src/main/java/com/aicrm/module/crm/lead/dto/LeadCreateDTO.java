package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建线索DTO
 */
@Data
@Schema(description = "创建线索请求")
public class LeadCreateDTO {

    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "来源明细")
    private String sourceDetail;

    @Schema(description = "意向等级 A/B/C/D")
    private String intentionLevel;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "备注")
    private String remark;
}
