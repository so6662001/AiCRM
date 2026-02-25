package com.aicrm.module.crm.customer.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.customer.dto.CustomerCreateDTO;
import com.aicrm.module.crm.customer.dto.CustomerQueryDTO;
import com.aicrm.module.crm.customer.dto.CustomerUpdateDTO;
import com.aicrm.module.crm.customer.dto.CustomerVO;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 分页查询客户
     */
    PageResult<CustomerVO> page(CustomerQueryDTO query);

    /**
     * 根据ID获取客户详情
     */
    CustomerVO getById(Long id);

    /**
     * 创建客户
     */
    Long create(CustomerCreateDTO dto);

    /**
     * 更新客户
     */
    void update(CustomerUpdateDTO dto);

    /**
     * 删除客户
     */
    void delete(Long id);

    /**
     * 批量转移客户
     */
    void transfer(List<Long> customerIds, Long targetUserId);

    /**
     * 标记客户无效
     */
    void markInvalid(Long id, String reason);

    /**
     * 重新激活客户
     */
    void reactivate(Long id);
}
