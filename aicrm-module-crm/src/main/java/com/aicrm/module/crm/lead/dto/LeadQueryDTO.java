package com.aicrm.module.crm.lead.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 线索查询参数DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "线索查询参数")
public class LeadQueryDTO extends PageQuery {

    @Schema(description = "状态 0待分配/1已分配/2跟进中/3已转化/4已退回/5无效")
    private Integer status;

    @Schema(description = "意向等级 A/B/C/D")
    private String intentionLevel;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "负责人用户ID")
    private Long ownerUserId;

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "是否在公海池")
    private Boolean inPool;

    @Schema(description = "创建时间开始")
    private LocalDateTime createdTimeStart;

    @Schema(description = "创建时间结束")
    private LocalDateTime createdTimeEnd;
}
