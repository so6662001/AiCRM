package com.aicrm.module.crm.followup.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 跟进记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("follow_up_record")
public class FollowUpRecord extends BaseEntity {

    /**
     * 业务类型 1线索/2客户/3商机
     */
    private Integer bizType;
    /**
     * 业务ID
     */
    private Long bizId;
    /**
     * 客户ID
     */
    private Long customerId;
    /**
     * 跟进方式 1现场拜访/2电话/3微信/4邮件/5企业微信/6其他
     */
    private Integer followType;
    /**
     * 跟进内容
     */
    private String content;
    /**
     * 拜访ID
     */
    private Long visitId;
    /**
     * 下次跟进时间
     */
    private LocalDateTime nextFollowTime;
    /**
     * 下次跟进备注
     */
    private String nextFollowNote;
    /**
     * 是否有录音 0否/1是
     */
    private Integer hasRecording;
    /**
     * 录音文件ID
     */
    private Long recordingFileId;
    /**
     * AI摘要
     */
    private String aiSummary;
    /**
     * AI分析
     */
    private String aiAnalysis;
    /**
     * 是否违规 0否/1是
     */
    private Integer hasViolation;
    /**
     * 违规详情
     */
    private String violationDetail;
    /**
     * 跟进人用户ID
     */
    private Long followUserId;
    /**
     * 跟进人组织ID
     */
    private Long followUserOrgId;
}
