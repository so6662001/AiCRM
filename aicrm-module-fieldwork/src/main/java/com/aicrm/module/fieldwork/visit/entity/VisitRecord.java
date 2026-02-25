package com.aicrm.module.fieldwork.visit.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拜访记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visit_record")
public class VisitRecord extends BaseEntity {

    /** 拜访编号 */
    private String visitNo;
    /** 客户ID */
    private Long customerId;
    /** 联系人ID */
    private Long contactId;
    /** 拜访类型 1现场/2电话/3微信/4企微/5视频 */
    private Integer visitType;
    /** 拜访目的 */
    private String visitPurpose;
    /** 拜访结果 */
    private String visitResult;
    /** 拜访开始时间 */
    private LocalDateTime visitTime;
    /** 拜访结束时间 */
    private LocalDateTime visitEndTime;
    /** 拜访时长(分钟) */
    private Integer visitDuration;
    /** 签到地址 */
    private String checkinAddress;
    /** 签到经度 */
    private BigDecimal checkinLongitude;
    /** 签到纬度 */
    private BigDecimal checkinLatitude;
    /** 签到时间 */
    private LocalDateTime checkinTime;
    /** 签到照片URL */
    private String checkinPhotoUrl;
    /** 签退时间 */
    private LocalDateTime checkoutTime;
    /** 通话号码 */
    private String callPhone;
    /** 通话时长(秒) */
    private Integer callDuration;
    /** 录音文件ID */
    private Long recordingFileId;
    /** 录音URL */
    private String recordingUrl;
    /** AI摘要 */
    private String aiSummary;
    /** 是否违规 0否/1是 */
    private Integer hasViolation;
    /** 聊天截图URLs JSON */
    private String chatScreenshotUrls;
    /** 状态 1计划中/2进行中/3已完成/4已取消 */
    private Integer status;
    /** 拜访人用户ID */
    private Long visitorUserId;
    /** 拜访人组织ID */
    private Long visitorOrgId;
    /** 跟进记录ID */
    private Long followUpId;
    /** 备注 */
    private String remark;
}
