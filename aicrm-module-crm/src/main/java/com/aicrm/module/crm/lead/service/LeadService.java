package com.aicrm.module.crm.lead.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.lead.dto.*;

import java.util.List;

/**
 * 线索服务接口
 */
public interface LeadService {

    /**
     * 分页查询线索
     */
    PageResult<LeadVO> page(LeadQueryDTO query);

    /**
     * 根据ID获取线索详情
     */
    LeadVO getById(Long id);

    /**
     * 创建线索
     */
    Long create(LeadCreateDTO dto);

    /**
     * 更新线索
     */
    void update(LeadUpdateDTO dto);

    /**
     * 删除线索（逻辑删除）
     */
    void delete(Long id);

    /**
     * 分配线索
     */
    void assign(List<Long> leadIds, Long targetUserId);

    /**
     * 退回线索到公海池
     */
    void returnToPool(Long id, String reason);

    /**
     * 线索转化
     * @param id 线索ID
     * @param customerName 客户名称
     * @param createOpportunity 是否创建商机（暂未实现）
     * @return 转化后的客户ID
     */
    Long convert(Long id, String customerName, boolean createOpportunity);
}
