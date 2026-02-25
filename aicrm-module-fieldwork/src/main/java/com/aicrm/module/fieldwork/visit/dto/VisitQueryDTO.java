package com.aicrm.module.fieldwork.visit.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 拜访查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "拜访查询条件")
public class VisitQueryDTO extends PageQuery {

    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "拜访类型 1现场/2电话/3微信/4企微/5视频")
    private Integer visitType;
    @Schema(description = "状态 1计划中/2进行中/3已完成/4已取消")
    private Integer status;
    @Schema(description = "拜访人用户ID")
    private Long visitorUserId;
    @Schema(description = "拜访时间开始")
    private LocalDateTime visitTimeStart;
    @Schema(description = "拜访时间结束")
    private LocalDateTime visitTimeEnd;
}
