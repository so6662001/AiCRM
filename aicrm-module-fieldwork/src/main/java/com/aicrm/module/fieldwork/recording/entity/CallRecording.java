package com.aicrm.module.fieldwork.recording.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 电话录音实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("call_recording")
public class CallRecording extends BaseEntity {

    /** 用户ID */
    private Long userId;
    /** 客户ID */
    private Long customerId;
    /** 联系人ID */
    private Long contactId;
    /** 拜访ID */
    private Long visitId;
    /** 通话类型 1呼出/2呼入 */
    private Integer callType;
    /** 主叫号码 */
    private String callerNumber;
    /** 被叫号码 */
    private String calleeNumber;
    /** 通话开始时间 */
    private LocalDateTime callStartTime;
    /** 通话结束时间 */
    private LocalDateTime callEndTime;
    /** 通话时长(秒) */
    private Integer callDuration;
    /** 录音文件URL */
    private String recordingFileUrl;
    /** 录音文件大小 */
    private Long recordingFileSize;
    /** 录音格式 */
    private String recordingFormat;
    /** 转写状态 0未转写/1转写中/2已转写/3失败 */
    private Integer transcriptionStatus;
    /** 转写文本 */
    private String transcriptionText;
    /** AI摘要 */
    private String aiSummary;
    /** AI关键词 */
    private String aiKeywords;
    /** AI情感分析 */
    private String aiSentiment;
    /** 违规检测状态 0未检/1检中/2已检 */
    private Integer violationCheckStatus;
    /** 是否违规 0否/1是 */
    private Integer hasViolation;
    /** 违规详情 */
    private String violationDetail;
    /** 违规等级 */
    private Integer violationLevel;
    /** 来源 */
    private String source;
}
