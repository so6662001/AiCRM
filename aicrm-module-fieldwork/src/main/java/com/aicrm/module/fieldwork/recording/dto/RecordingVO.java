package com.aicrm.module.fieldwork.recording.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 录音视图VO
 */
@Data
@Schema(description = "录音视图")
public class RecordingVO {

    private Long id;
    private Long tenantId;
    private Long userId;
    private Long customerId;
    private Long contactId;
    private Long visitId;
    private Integer callType;
    private String callerNumber;
    private String calleeNumber;
    private LocalDateTime callStartTime;
    private LocalDateTime callEndTime;
    private Integer callDuration;
    private String recordingFileUrl;
    private Long recordingFileSize;
    private String recordingFormat;
    private Integer transcriptionStatus;
    private String transcriptionText;
    private String aiSummary;
    private String aiKeywords;
    private String aiSentiment;
    private Integer violationCheckStatus;
    private Integer hasViolation;
    private String violationDetail;
    private Integer violationLevel;
    private String source;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
