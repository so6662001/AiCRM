package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动视图对象
 */
@Data
@Schema(description = "活动视图对象")
public class ActivityVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "活动编号")
    private String activityNo;
    @Schema(description = "活动名称")
    private String activityName;
    @Schema(description = "活动类别 1需要报名/2不需要报名")
    private Integer activityCategory;
    @Schema(description = "活动类别标签")
    private String categoryLabel;
    @Schema(description = "活动类型 1线下展会/2线上推广/3产品发布/4客户沙龙/5培训会议/6品牌宣传/7优惠活动/8内容分发/9其他")
    private Integer activityType;
    @Schema(description = "活动描述")
    private String description;
    @Schema(description = "富文本内容")
    private String richContent;
    @Schema(description = "封面图URL")
    private String coverImageUrl;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;
    @Schema(description = "报名截止时间")
    private LocalDateTime registrationEndTime;
    @Schema(description = "最大参与人数")
    private Integer maxParticipants;
    @Schema(description = "当前参与人数")
    private Integer currentParticipants;
    @Schema(description = "报名审核 0自动通过/1需审核")
    private Integer registrationApproval;
    @Schema(description = "报名须知")
    private String registrationNotice;
    @Schema(description = "是否启用候补 0/1")
    private Integer waitlistEnabled;
    @Schema(description = "活动地点")
    private String location;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "线上活动URL")
    private String onlineUrl;
    @Schema(description = "二维码图片URL")
    private String qrCodeUrl;
    @Schema(description = "二维码内容")
    private String qrCodeContent;
    @Schema(description = "总扫码次数")
    private Integer totalScans;
    @Schema(description = "独立扫码人数")
    private Integer uniqueScans;
    @Schema(description = "状态 0草稿/1未开始/2报名中/3报名截止/4进行中/5已结束/6已取消")
    private Integer status;
    @Schema(description = "状态标签")
    private String statusLabel;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "负责人姓名")
    private String ownerUserName;
    @Schema(description = "负责人组织ID")
    private Long ownerOrgId;
    @Schema(description = "预算")
    private BigDecimal budget;
    @Schema(description = "实际成本")
    private BigDecimal actualCost;
    @Schema(description = "企微绑定 0/1")
    private Integer wechatWorkBind;
    @Schema(description = "企微二维码URL")
    private String wechatWorkQrUrl;
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
