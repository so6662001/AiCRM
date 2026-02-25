package com.aicrm.module.fieldwork.trajectory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 位置上报实体（轻量级表，不继承BaseEntity）
 */
@Data
@TableName("location_report")
public class LocationReport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long userId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Float accuracy;
    private String address;
    private LocalDateTime reportTime;
    /** 上报类型 1定时/2打卡/3拜访 */
    private Integer reportType;
    private String relatedBizType;
    private Long relatedBizId;
    private Integer batteryLevel;
    private String networkType;
    private String deviceInfo;
    private LocalDateTime createdTime;
}
