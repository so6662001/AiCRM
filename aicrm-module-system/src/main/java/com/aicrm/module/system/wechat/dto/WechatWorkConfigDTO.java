package com.aicrm.module.system.wechat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 企业微信配置DTO
 */
@Data
public class WechatWorkConfigDTO implements Serializable {

    @NotBlank(message = "企业ID不能为空")
    private String corpId;
    @NotBlank(message = "应用ID不能为空")
    private String agentId;
    @NotBlank(message = "应用Secret不能为空")
    private String secret;
    private String contactSecret;
    private String customerSecret;
    private String callbackToken;
    private String callbackAesKey;
    private String welcomeMsg;
}
