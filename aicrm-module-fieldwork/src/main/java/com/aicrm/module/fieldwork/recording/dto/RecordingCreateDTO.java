package com.aicrm.module.fieldwork.recording.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建录音DTO
 */
@Data
@Schema(description = "创建录音请求")
public class RecordingCreateDTO {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "联系人ID")
    private Long contactId;
    @Schema(description = "拜访ID")
    private Long visitId;
    @Schema(description = "通话类型 1呼出/2呼入")
    private Integer callType;
    @Schema(description = "主叫号码")
    private String callerNumber;
    @Schema(description = "被叫号码")
    private String calleeNumber;
    @Schema(description = "通话开始时间")
    private LocalDateTime callStartTime;
    @Schema(description = "通话结束时间")
    private LocalDateTime callEndTime;
    @Schema(description = "通话时长(秒)")
    private Integer callDuration;
    @Schema(description = "录音文件URL")
    private String recordingFileUrl;
    @Schema(description = "录音文件大小")
    private Long recordingFileSize;
    @Schema(description = "录音格式")
    private String recordingFormat;
    @Schema(description = "来源")
    private String source;
}
