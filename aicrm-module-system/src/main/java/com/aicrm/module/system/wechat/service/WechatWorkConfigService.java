package com.aicrm.module.system.wechat.service;

import com.aicrm.module.system.wechat.dto.WechatWorkConfigDTO;
import com.aicrm.module.system.wechat.dto.WechatWorkConfigVO;

/**
 * 企业微信配置服务
 */
public interface WechatWorkConfigService {

    WechatWorkConfigVO getConfig();

    Long saveConfig(WechatWorkConfigDTO dto);

    void updateConfig(Long id, WechatWorkConfigDTO dto);

    boolean testConnection(Long id);
}
