package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公开活动信息VO（扫码获取，不含敏感字段）
 */
@Data
@Schema(description = "公开活动信息")
public class OpenActivityInfoVO {

    private Long id;
    private String activityNo;
    private String activityName;
    private Integer activityCategory;
    private Integer activityType;
    private String description;
    private String richContent;
    private String coverImageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer registrationApproval;
    private String registrationNotice;
    private Integer waitlistEnabled;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String onlineUrl;
    private String qrCodeUrl;
    private Integer totalScans;
    private Integer uniqueScans;
    private Integer status;
}
