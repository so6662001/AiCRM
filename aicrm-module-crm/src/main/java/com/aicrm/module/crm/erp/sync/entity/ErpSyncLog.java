package com.aicrm.module.crm.erp.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP同步日志实体（轻量表，不继承BaseEntity）
 */
@Data
@TableName("erp_sync_log")
public class ErpSyncLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** ERP配置ID */
    private Long erpConfigId;
    /** 同步类型 1:ERP→CRM 2:CRM→ERP */
    private Integer syncType;
    /** 同步模式 1全量/2增量/3单条 */
    private Integer syncMode;
    /** 业务类型 customer/product/order */
    private String bizType;
    /** 总数量 默认0 */
    private Integer totalCount;
    /** 成功数量 默认0 */
    private Integer successCount;
    /** 失败数量 默认0 */
    private Integer failCount;
    /** 跳过数量 默认0 */
    private Integer skipCount;
    /** 状态 1进行中/2成功/3部分失败/4失败 */
    private Integer status;
    /** 错误信息 */
    private String errorMessage;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 操作人 */
    private Long operatedBy;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
