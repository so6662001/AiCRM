package com.aicrm.module.fieldwork.checkin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 签到记录VO
 */
@Data
@Schema(description = "签到记录视图")
public class CheckinVO {

    private Long id;
    private Long tenantId;
    private Long userId;
    private String userName;
    private Integer checkinType;
    private LocalDateTime checkinTime;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String photoUrl;
    private String wifiName;
    private String deviceInfo;
    private Long relatedVisitId;
    private String remark;
    private LocalDateTime createdTime;
}
