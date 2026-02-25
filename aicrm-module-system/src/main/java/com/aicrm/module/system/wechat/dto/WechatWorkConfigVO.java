package com.aicrm.module.system.wechat.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业微信配置VO - 敏感字段脱敏显示
 */
@Data
public class WechatWorkConfigVO implements Serializable {

    private Long id;
    private String corpId;
    private String agentId;
    private String secret;
    private String contactSecret;
    private String customerSecret;
    private String callbackToken;
    private String callbackAesKey;
    private String contactWayId;
    private String welcomeMsg;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
