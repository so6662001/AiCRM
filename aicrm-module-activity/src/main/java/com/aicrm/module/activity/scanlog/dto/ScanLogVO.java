package com.aicrm.module.activity.scanlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动扫码日志视图VO
 */
@Data
@Schema(description = "活动扫码日志视图")
public class ScanLogVO {

    private Long id;
    private Long tenantId;
    private Long activityId;
    private String scanFingerprint;
    private String scanIp;
    private String scanUa;
    private LocalDateTime scanTime;
    private String referer;
    private Integer didRegister;
    private Integer didAddFriend;
    private Long participantId;
    private LocalDateTime createdTime;
}
