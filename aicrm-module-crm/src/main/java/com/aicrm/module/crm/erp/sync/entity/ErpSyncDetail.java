package com.aicrm.module.crm.erp.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP同步明细实体（轻量表，不继承BaseEntity）
 */
@Data
@TableName("erp_sync_detail")
public class ErpSyncDetail implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 同步日志ID */
    private Long syncLogId;
    /** CRM业务ID */
    private Long crmBizId;
    /** ERP业务ID */
    private String erpBizId;
    /** 同步动作 1新增/2更新/3跳过 */
    private Integer syncAction;
    /** 状态 1成功/2失败/3跳过 */
    private Integer status;
    /** 请求数据 */
    private String requestData;
    /** 响应数据 */
    private String responseData;
    /** 错误信息 */
    private String errorMessage;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
