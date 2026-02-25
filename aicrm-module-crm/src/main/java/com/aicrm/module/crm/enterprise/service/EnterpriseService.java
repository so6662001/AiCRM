package com.aicrm.module.crm.enterprise.service;

import com.aicrm.module.crm.enterprise.dto.ContactImportDTO;
import com.aicrm.module.crm.enterprise.dto.ContactQuotaVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseContactVO;
import com.aicrm.module.crm.enterprise.dto.EnterpriseSearchVO;

import java.util.List;

/**
 * 五度易链企业信息服务
 */
public interface EnterpriseService {

    /**
     * 搜索企业（模拟五度易链API）
     */
    List<EnterpriseSearchVO> search(String keyword);

    /**
     * 查询企业联系人（先查缓存，无则调API模拟）
     */
    List<EnterpriseContactVO> queryContacts(String creditCode);

    /**
     * 仅查本地缓存联系人，不调API
     */
    List<EnterpriseContactVO> getContactsCache(String creditCode);

    /**
     * 导入联系人到CRM
     */
    void importContact(String creditCode, Long contactId, ContactImportDTO dto);

    /**
     * 获取联系人配额
     */
    ContactQuotaVO getContactQuota();
}
