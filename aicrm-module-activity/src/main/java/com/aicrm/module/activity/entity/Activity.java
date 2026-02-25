package com.aicrm.module.activity.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {

    /** 活动编号 */
    private String activityNo;
    /** 活动名称 */
    private String activityName;
    /** 活动类别 1需要报名/2不需要报名 */
    private Integer activityCategory;
    /** 活动类型 1线下展会/2线上推广/3产品发布/4客户沙龙/5培训会议/6品牌宣传/7优惠活动/8内容分发/9其他 */
    private Integer activityType;
    /** 活动描述 */
    private String description;
    /** 富文本内容 */
    private String richContent;
    /** 封面图URL */
    private String coverImageUrl;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 报名开始时间 */
    private LocalDateTime registrationStartTime;
    /** 报名截止时间 */
    private LocalDateTime registrationEndTime;
    /** 最大参与人数 */
    private Integer maxParticipants;
    /** 当前参与人数 默认0 */
    private Integer currentParticipants;
    /** 报名审核 0自动通过/1需审核 */
    private Integer registrationApproval;
    /** 报名须知 */
    private String registrationNotice;
    /** 是否启用候补 0/1 默认0 */
    private Integer waitlistEnabled;
    /** 活动地点 */
    private String location;
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
    /** 线上活动URL */
    private String onlineUrl;
    /** 二维码图片URL */
    private String qrCodeUrl;
    /** 二维码内容 */
    private String qrCodeContent;
    /** 总扫码次数 默认0 */
    private Integer totalScans;
    /** 独立扫码人数 默认0 */
    private Integer uniqueScans;
    /** 状态 0草稿/1未开始/2报名中/3报名截止/4进行中/5已结束/6已取消 */
    private Integer status;
    /** 负责人用户ID */
    private Long ownerUserId;
    /** 负责人组织ID */
    private Long ownerOrgId;
    /** 预算 */
    private BigDecimal budget;
    /** 实际成本 */
    private BigDecimal actualCost;
    /** 企微绑定 0/1 默认0 */
    private Integer wechatWorkBind;
    /** 企微二维码URL */
    private String wechatWorkQrUrl;
    /** 备注 */
    private String remark;
}
