package com.aicrm.module.activity.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 活动参与人实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_participant")
public class ActivityParticipant extends BaseEntity {

    /** 活动ID */
    private Long activityId;
    /** 参与人姓名 */
    private String participantName;
    /** 参与人电话 */
    private String participantPhone;
    /** 参与人邮箱 */
    private String participantEmail;
    /** 公司名称 */
    private String companyName;
    /** 职位 */
    private String position;
    /** 来源 1扫码报名/2手动录入/3批量导入/4扫码参与 */
    private Integer source;
    /** 扫码时间 */
    private LocalDateTime scanTime;
    /** 报名状态 0待审核/1已通过/2已拒绝/3已取消/4候补 */
    private Integer registrationStatus;
    /** 报名时间 */
    private LocalDateTime registrationTime;
    /** 拒绝原因 */
    private String rejectReason;
    /** 签到状态 0未签到/1已签到 */
    private Integer checkinStatus;
    /** 签到时间 */
    private LocalDateTime checkinTime;
    /** 是否客户 0/1 */
    private Integer isCustomer;
    /** 客户ID */
    private Long customerId;
    /** 是否线索 0/1 */
    private Integer isLead;
    /** 线索ID */
    private Long leadId;
    /** 是否加好友 0/1 */
    private Integer friendAdded;
    /** 好友ID */
    private Long friendId;
    /** 是否加微信 0/1 */
    private Integer wechatAdded;
    /** 反馈内容 */
    private String feedback;
    /** 反馈评分 1~5 */
    private Integer feedbackScore;
    /** 备注 */
    private String remark;
}
