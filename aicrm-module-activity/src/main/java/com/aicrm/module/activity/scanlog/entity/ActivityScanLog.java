package com.aicrm.module.activity.scanlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动扫码日志实体（轻量表，不继承BaseEntity）
 */
@Data
@TableName("activity_scan_log")
public class ActivityScanLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 活动ID */
    private Long activityId;
    /** 扫码指纹(ip+ua hash) */
    private String scanFingerprint;
    /** 扫码IP */
    private String scanIp;
    /** 扫码UA */
    private String scanUa;
    /** 扫码时间 */
    private LocalDateTime scanTime;
    /** 来源页 */
    private String referer;
    /** 是否注册 0/1 默认0 */
    private Integer didRegister;
    /** 是否加好友 0/1 默认0 */
    private Integer didAddFriend;
    /** 参与人ID */
    private Long participantId;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
