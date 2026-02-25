package com.aicrm.module.crm.lead.pool.service;

import com.aicrm.module.crm.lead.pool.dto.PoolConfigDTO;
import com.aicrm.module.crm.lead.pool.dto.PoolConfigVO;

import java.util.List;

/**
 * 公海池配置服务
 */
public interface LeadPoolConfigService {

    /**
     * 查询公海池配置列表
     */
    List<PoolConfigVO> list();

    /**
     * 创建公海池配置
     */
    Long create(PoolConfigDTO dto);

    /**
     * 更新公海池配置
     */
    void update(Long id, PoolConfigDTO dto);
}
