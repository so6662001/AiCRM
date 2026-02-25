package com.aicrm.module.crm.erp.config.service;

import com.aicrm.module.crm.erp.config.dto.ErpConfigDTO;
import com.aicrm.module.crm.erp.config.dto.ErpConfigVO;

import java.util.List;

/**
 * ERP配置服务
 */
public interface ErpConfigService {

    List<ErpConfigVO> list();

    ErpConfigVO getById(Long id);

    Long create(ErpConfigDTO dto);

    void update(Long id, ErpConfigDTO dto);

    void delete(Long id);

    boolean testConnection(Long id);
}
