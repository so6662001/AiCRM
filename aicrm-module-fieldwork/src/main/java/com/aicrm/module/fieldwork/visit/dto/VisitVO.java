package com.aicrm.module.fieldwork.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拜访记录VO
 */
@Data
@Schema(description = "拜访记录视图")
public class VisitVO {

    private Long id;
    private Long tenantId;
    private String visitNo;
    private Long customerId;
    private String customerName;
    private Long contactId;
    private Integer visitType;
    private String visitPurpose;
    private String visitResult;
    private LocalDateTime visitTime;
    private LocalDateTime visitEndTime;
    private Integer visitDuration;
    private String checkinAddress;
    private BigDecimal checkinLongitude;
    private BigDecimal checkinLatitude;
    private LocalDateTime checkinTime;
    private String checkinPhotoUrl;
    private LocalDateTime checkoutTime;
    private String callPhone;
    private Integer callDuration;
    private Long recordingFileId;
    private String recordingUrl;
    private String aiSummary;
    private Integer hasViolation;
    private String chatScreenshotUrls;
    private Integer status;
    private Long visitorUserId;
    private String visitorUserName;
    private Long visitorOrgId;
    private Long followUpId;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
