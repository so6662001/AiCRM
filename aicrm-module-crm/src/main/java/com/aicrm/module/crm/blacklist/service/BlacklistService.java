package com.aicrm.module.crm.blacklist.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.blacklist.dto.BlacklistCreateDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistQueryDTO;
import com.aicrm.module.crm.blacklist.dto.BlacklistVO;

/**
 * 黑名单服务
 */
public interface BlacklistService {

    PageResult<BlacklistVO> page(BlacklistQueryDTO query);

    Long addToBlacklist(BlacklistCreateDTO dto);

    void release(Long id, String releaseReason);

    boolean isBlacklisted(String companyName, String phone);
}
