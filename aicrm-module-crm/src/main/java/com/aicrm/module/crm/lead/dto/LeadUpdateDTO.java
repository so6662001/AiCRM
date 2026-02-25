package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新线索DTO
 */
@Data
@Schema(description = "更新线索请求")
public class LeadUpdateDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "线索ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "联系人姓名")
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
