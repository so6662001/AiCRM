package com.aicrm.module.crm.erp.config.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户ERP配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_erp_config")
public class TenantErpConfig extends BaseEntity {

    /** ERP类型 */
    private String erpType;
    /** ERP名称 */
    private String erpName;
    /** API基础URL */
    private String apiBaseUrl;
    /** 认证类型 默认api_key */
    private String authType;
    /** 认证配置JSON */
    private String authConfig;
    /** 字段映射JSON */
    private String fieldMapping;
    /** 同步策略 默认manual */
    private String syncStrategy;
    /** 同步Cron表达式 */
    private String syncCron;
    /** 状态 */
    private Integer status;
    /** 最后同步时间 */
    private LocalDateTime lastSyncTime;
}
