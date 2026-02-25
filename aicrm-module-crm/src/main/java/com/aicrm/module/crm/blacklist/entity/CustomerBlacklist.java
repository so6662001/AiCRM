package com.aicrm.module.crm.blacklist.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客户黑名单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_blacklist")
public class CustomerBlacklist extends BaseEntity {

    /** 客户ID */
    private Long customerId;
    /** 企业名称 */
    private String companyName;
    /** 统一社会信用代码 */
    private String creditCode;
    /** 联系电话 */
    private String contactPhone;
    /** 黑名单类型 1欺诈/2恶意投诉/3空壳/4竞对/5其他 */
    private Integer blacklistType;
    /** 原因说明 */
    private String reason;
    /** 证据URL列表 JSON */
    private String evidenceUrls;
    /** 状态 1生效/2已解除 */
    private Integer status;
    /** 操作人 */
    private Long operatedBy;
    /** 解除人 */
    private Long releaseBy;
    /** 解除时间 */
    private LocalDateTime releaseTime;
    /** 解除原因 */
    private String releaseReason;
}
