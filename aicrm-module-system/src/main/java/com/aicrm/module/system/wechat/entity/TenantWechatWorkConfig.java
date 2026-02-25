package com.aicrm.module.system.wechat.entity;

import com.aicrm.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户企业微信配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_wechat_work_config")
public class TenantWechatWorkConfig extends BaseEntity {

    /** 企业ID */
    private String corpId;
    /** 应用ID */
    private String agentId;
    /** 应用Secret */
    private String secret;
    /** 通讯录Secret */
    private String contactSecret;
    /** 客户联系Secret */
    private String customerSecret;
    /** 回调Token */
    private String callbackToken;
    /** 回调AES密钥 */
    private String callbackAesKey;
    /** 联系我方式ID */
    private String contactWayId;
    /** 欢迎语 */
    private String welcomeMsg;
    /** 状态 默认1 */
    private Integer status = 1;
}
