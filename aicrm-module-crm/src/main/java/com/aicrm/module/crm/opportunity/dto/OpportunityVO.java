package com.aicrm.module.crm.opportunity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商机视图对象
 */
@Data
@Schema(description = "商机视图对象")
public class OpportunityVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "商机编号")
    private String opportunityNo;
    @Schema(description = "商机名称")
    private String opportunityName;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "联系人ID")
    private Long contactId;
    @Schema(description = "阶段ID")
    private Long stageId;
    @Schema(description = "阶段名称")
    private String stageName;
    @Schema(description = "预计金额")
    private BigDecimal expectedAmount;
    @Schema(description = "实际成交金额")
    private BigDecimal actualAmount;
    @Schema(description = "赢率（%）")
    private Integer winRate;
    @Schema(description = "预计成交日期")
    private LocalDate expectedCloseDate;
    @Schema(description = "实际成交日期")
    private LocalDate actualCloseDate;
    @Schema(description = "状态 1进行中/2赢单/3输单/4无效")
    private Integer status;
    @Schema(description = "输单原因")
    private String lossReason;
    @Schema(description = "竞争对手")
    private String competitor;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "负责人姓名")
    private String ownerUserName;
    @Schema(description = "负责人组织ID")
    private Long ownerOrgId;
    @Schema(description = "最后跟进时间")
    private LocalDateTime lastFollowTime;
    @Schema(description = "跟进次数")
    private Integer followCount;
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
