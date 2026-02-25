package com.aicrm.module.crm.customer.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户查询条件")
public class CustomerQueryDTO extends PageQuery {

    @Schema(description = "关键词(客户名称/简称模糊搜索)")
    private String keyword;
    @Schema(description = "生命周期阶段 1潜在/2意向/3成交/4活跃/5VIP/6流失/7无效/8黑名单")
    private Integer lifecycleStage;
    @Schema(description = "客户等级 A/B/C/D")
    private String level;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "行业")
    private String industry;
    @Schema(description = "采购状态: upcoming/overdue")
    private String purchaseStatus;
    @Schema(description = "采购天数范围内(天)")
    private Integer purchaseDaysWithin;
    @Schema(description = "创建时间开始")
    private LocalDateTime createdTimeStart;
    @Schema(description = "创建时间结束")
    private LocalDateTime createdTimeEnd;
}
