package com.aicrm.module.crm.opportunity.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 商机查询参数DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商机查询参数")
public class OpportunityQueryDTO extends PageQuery {

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "阶段ID")
    private Long stageId;

    @Schema(description = "状态 1进行中/2赢单/3输单/4无效")
    private Integer status;

    @Schema(description = "负责人用户ID")
    private Long ownerUserId;

    @Schema(description = "预计成交日期开始")
    private LocalDate expectedCloseDateStart;

    @Schema(description = "预计成交日期结束")
    private LocalDate expectedCloseDateEnd;
}
