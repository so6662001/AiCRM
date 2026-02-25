package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建活动DTO
 */
@Data
@Schema(description = "创建活动请求")
public class ActivityCreateDTO {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityName;

    @NotNull(message = "活动类别不能为空")
    @Schema(description = "活动类别 1需要报名/2不需要报名", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityCategory;

    @NotNull(message = "活动类型不能为空")
    @Schema(description = "活动类型 1线下展会/2线上推广/3产品发布/4客户沙龙/5培训会议/6品牌宣传/7优惠活动/8内容分发/9其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityType;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "富文本内容")
    private String richContent;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationEndTime;

    @Schema(description = "最大参与人数")
    private Integer maxParticipants;

    @Schema(description = "报名审核 0自动通过/1需审核")
    private Integer registrationApproval;

    @Schema(description = "报名须知")
    private String registrationNotice;

    @Schema(description = "是否启用候补 0/1")
    private Integer waitlistEnabled;

    @Schema(description = "活动地点")
    private String location;

    @Schema(description = "线上活动URL")
    private String onlineUrl;

    @Schema(description = "预算")
    private BigDecimal budget;

    @Schema(description = "企微绑定 0/1")
    private Integer wechatWorkBind;
}
