package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 客户创建DTO
 */
@Data
@Schema(description = "客户创建请求")
public class CustomerCreateDTO {

    @NotBlank(message = "客户名称不能为空")
    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerName;

    @NotNull(message = "客户类型不能为空")
    @Schema(description = "客户类型 1企业/2个人", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer customerType;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "规模")
    private String scale;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "负责人用户ID")
    private Long ownerUserId;

    @Schema(description = "预计采购日期")
    private LocalDate expectedPurchaseDate;
}
