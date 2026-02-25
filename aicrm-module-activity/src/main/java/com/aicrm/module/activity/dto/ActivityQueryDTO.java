package com.aicrm.module.activity.dto;

import com.aicrm.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动查询参数DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "活动查询参数")
public class ActivityQueryDTO extends PageQuery {

    @Schema(description = "活动类别 1需要报名/2不需要报名")
    private Integer activityCategory;

    @Schema(description = "活动类型 1线下展会/2线上推广/3产品发布/4客户沙龙/5培训会议/6品牌宣传/7优惠活动/8内容分发/9其他")
    private Integer activityType;

    @Schema(description = "状态 0草稿/1未开始/2报名中/3报名截止/4进行中/5已结束/6已取消")
    private Integer status;

    @Schema(description = "负责人用户ID")
    private Long ownerUserId;

    @Schema(description = "关键词搜索(活动名称)")
    private String keyword;
}
