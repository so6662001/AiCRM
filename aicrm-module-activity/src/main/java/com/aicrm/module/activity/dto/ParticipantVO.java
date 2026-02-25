package com.aicrm.module.activity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参与人视图对象
 */
@Data
@Schema(description = "参与人视图对象")
public class ParticipantVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "活动ID")
    private Long activityId;
    @Schema(description = "参与人姓名")
    private String participantName;
    @Schema(description = "参与人电话")
    private String participantPhone;
    @Schema(description = "参与人邮箱")
    private String participantEmail;
    @Schema(description = "公司名称")
    private String companyName;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "来源 1扫码报名/2手动录入/3批量导入/4扫码参与")
    private Integer source;
    @Schema(description = "扫码时间")
    private LocalDateTime scanTime;
    @Schema(description = "报名状态 0待审核/1已通过/2已拒绝/3已取消/4候补")
    private Integer registrationStatus;
    @Schema(description = "报名时间")
    private LocalDateTime registrationTime;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "签到状态 0未签到/1已签到")
    private Integer checkinStatus;
    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;
    @Schema(description = "是否客户 0/1")
    private Integer isCustomer;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "是否线索 0/1")
    private Integer isLead;
    @Schema(description = "线索ID")
    private Long leadId;
    @Schema(description = "是否加好友 0/1")
    private Integer friendAdded;
    @Schema(description = "好友ID")
    private Long friendId;
    @Schema(description = "是否加微信 0/1")
    private Integer wechatAdded;
    @Schema(description = "反馈内容")
    private String feedback;
    @Schema(description = "反馈评分 1~5")
    private Integer feedbackScore;
    @Schema(description = "备注")
    private String remark;
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
