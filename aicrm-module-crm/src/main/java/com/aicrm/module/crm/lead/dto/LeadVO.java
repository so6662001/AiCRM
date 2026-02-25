package com.aicrm.module.crm.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线索视图对象
 */
@Data
@Schema(description = "线索视图对象")
public class LeadVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "线索编号")
    private String leadNo;
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
    @Schema(description = "线索评分")
    private Integer leadScore;
    @Schema(description = "状态 0待分配/1已分配/2跟进中/3已转化/4已退回/5无效")
    private Integer status;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "负责人姓名")
    private String ownerUserName;
    @Schema(description = "负责人组织ID")
    private Long ownerOrgId;
    @Schema(description = "分配人用户ID")
    private Long assignUserId;
    @Schema(description = "分配时间")
    private LocalDateTime assignTime;
    @Schema(description = "首次跟进时间")
    private LocalDateTime firstFollowTime;
    @Schema(description = "最后跟进时间")
    private LocalDateTime lastFollowTime;
    @Schema(description = "跟进次数")
    private Integer followCount;
    @Schema(description = "转化时间")
    private LocalDateTime convertTime;
    @Schema(description = "转化客户ID")
    private Long convertCustomerId;
    @Schema(description = "退回原因")
    private String returnReason;
    @Schema(description = "是否在公海池 0否/1是")
    private Integer inPool;
    @Schema(description = "进入公海池时间")
    private LocalDateTime poolEnterTime;
    @Schema(description = "省份")
    private String province;
    @Schema(description = "城市")
    private String city;
    @Schema(description = "行业")
    private String industry;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
