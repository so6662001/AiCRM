package com.aicrm.module.system.tenant.service;

import com.aicrm.module.system.tenant.dto.TenantCreateDTO;
import com.aicrm.module.system.tenant.dto.TenantVO;

import java.util.List;

/**
 * 租户服务
 */
public interface TenantService {

    List<TenantVO> list();

    TenantVO getById(Long id);

    Long create(TenantCreateDTO dto);

    void update(Long id, TenantCreateDTO dto);

    void updateStatus(Long id, Integer status);
}
