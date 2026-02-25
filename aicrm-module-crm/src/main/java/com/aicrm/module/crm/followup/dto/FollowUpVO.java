package com.aicrm.module.crm.followup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进记录视图对象
 */
@Data
@Schema(description = "跟进记录视图对象")
public class FollowUpVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "业务类型 1线索/2客户/3商机")
    private Integer bizType;
    @Schema(description = "业务ID")
    private Long bizId;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "跟进方式 1现场拜访/2电话/3微信/4邮件/5企业微信/6其他")
    private Integer followType;
    @Schema(description = "跟进内容")
    private String content;
    @Schema(description = "拜访ID")
    private Long visitId;
    @Schema(description = "下次跟进时间")
    private LocalDateTime nextFollowTime;
    @Schema(description = "下次跟进备注")
    private String nextFollowNote;
    @Schema(description = "是否有录音 0否/1是")
    private Integer hasRecording;
    @Schema(description = "录音文件ID")
    private Long recordingFileId;
    @Schema(description = "AI摘要")
    private String aiSummary;
    @Schema(description = "AI分析")
    private String aiAnalysis;
    @Schema(description = "是否违规 0否/1是")
    private Integer hasViolation;
    @Schema(description = "违规详情")
    private String violationDetail;
    @Schema(description = "跟进人用户ID")
    private Long followUserId;
    @Schema(description = "跟进人组织ID")
    private Long followUserOrgId;
    @Schema(description = "跟进人姓名")
    private String followUserName;
    @Schema(description = "业务名称")
    private String bizName;
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
