package com.aicrm.module.system.org.service;

import com.aicrm.module.system.org.dto.OrgCreateDTO;
import com.aicrm.module.system.org.dto.OrgVO;

import java.util.List;

/**
 * 组织架构服务
 */
public interface OrganizationService {

    List<OrgVO> tree();

    OrgVO getById(Long id);

    Long create(OrgCreateDTO dto);

    void update(Long id, OrgCreateDTO dto);

    void delete(Long id);

    List<Object> listMembers(Long orgId);
}
