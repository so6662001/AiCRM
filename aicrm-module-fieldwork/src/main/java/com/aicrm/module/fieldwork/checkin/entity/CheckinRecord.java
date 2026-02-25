package com.aicrm.module.fieldwork.checkin.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 签到打卡记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_record")
public class CheckinRecord extends BaseEntity {

    /** 用户ID */
    private Long userId;
    /** 签到类型 1外出/2拜访签到/3日常 */
    private Integer checkinType;
    /** 签到时间 */
    private LocalDateTime checkinTime;
    /** 地址 */
    private String address;
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
    /** 照片URL */
    private String photoUrl;
    /** WiFi名称 */
    private String wifiName;
    /** 设备信息 */
    private String deviceInfo;
    /** 关联拜访ID */
    private Long relatedVisitId;
    /** 备注 */
    private String remark;
}
