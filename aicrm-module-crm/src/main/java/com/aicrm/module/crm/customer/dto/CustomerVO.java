package com.aicrm.module.crm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户视图对象
 */
@Data
@Schema(description = "客户详情")
public class CustomerVO {

    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "客户编号")
    private String customerNo;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "简称")
    private String shortName;
    @Schema(description = "客户类型 1企业/2个人")
    private Integer customerType;
    @Schema(description = "行业")
    private String industry;
    @Schema(description = "规模")
    private String scale;
    @Schema(description = "官网")
    private String website;
    @Schema(description = "省份")
    private String province;
    @Schema(description = "城市")
    private String city;
    @Schema(description = "区县")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "生命周期阶段 1潜在/2意向/3成交/4活跃/5VIP/6流失/7无效/8黑名单")
    private Integer lifecycleStage;
    @Schema(description = "客户等级 A/B/C/D")
    private String level;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "负责人组织ID")
    private Long ownerOrgId;
    @Schema(description = "来源线索ID")
    private Long sourceLeadId;
    @Schema(description = "来源")
    private String source;
    @Schema(description = "最后跟进时间")
    private LocalDateTime lastFollowTime;
    @Schema(description = "跟进次数")
    private Integer followCount;
    @Schema(description = "下次跟进时间")
    private LocalDateTime nextFollowTime;
    @Schema(description = "预计采购日期")
    private LocalDate expectedPurchaseDate;
    @Schema(description = "成交金额")
    private BigDecimal dealAmount;
    @Schema(description = "成交次数")
    private Integer dealCount;
    @Schema(description = "ERP客户ID")
    private String erpCustomerId;
    @Schema(description = "ERP同步状态 0未同步/1已同步/2同步中/3同步失败")
    private Integer erpSyncStatus;
    @Schema(description = "ERP最后同步时间")
    private LocalDateTime erpLastSyncTime;
    @Schema(description = "审批状态")
    private Integer approvalStatus;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "负责人用户名")
    private String ownerUserName;
    @Schema(description = "采购倒计时")
    private PurchaseCountdownVO purchaseCountdown;
}
