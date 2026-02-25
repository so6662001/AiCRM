package com.aicrm.module.crm.enterprise.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方配置实体（五度易链等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("third_party_config")
public class ThirdPartyConfig extends BaseEntity {

    /** 提供商 */
    private String provider;
    /** API基础URL */
    private String apiBaseUrl;
    /** AppKey */
    private String appKey;
    /** AppSecret */
    private String appSecret;
    /** 每日配额 */
    private Integer dailyQuota;
    /** 每日已用 */
    private Integer dailyUsed;
    /** 联系人每日配额 */
    private Integer contactDailyQuota;
    /** 联系人每日已用 */
    private Integer contactDailyUsed;
    /** 联系人每月配额 */
    private Integer contactMonthlyQuota;
    /** 联系人每月已用 */
    private Integer contactMonthlyUsed;
    /** 状态 */
    private Integer status;
}
