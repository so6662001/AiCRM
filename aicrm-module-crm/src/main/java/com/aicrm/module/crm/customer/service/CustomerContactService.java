package com.aicrm.module.crm.customer.service;

import com.aicrm.module.crm.customer.dto.ContactCreateDTO;
import com.aicrm.module.crm.customer.dto.ContactVO;

import java.util.List;

/**
 * 客户联系人服务接口
 */
public interface CustomerContactService {

    /**
     * 查询客户下的联系人列表
     */
    List<ContactVO> list(Long customerId);

    /**
     * 创建联系人
     */
    Long create(Long customerId, ContactCreateDTO dto);

    /**
     * 更新联系人
     */
    void update(Long customerId, Long contactId, ContactCreateDTO dto);

    /**
     * 删除联系人
     */
    void delete(Long customerId, Long contactId);
}
