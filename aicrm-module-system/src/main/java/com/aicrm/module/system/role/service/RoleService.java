package com.aicrm.module.system.role.service;

import com.aicrm.module.system.role.dto.RoleCreateDTO;
import com.aicrm.module.system.role.dto.RoleVO;

import java.util.List;

/**
 * 角色服务
 */
public interface RoleService {

    List<RoleVO> list();

    RoleVO getById(Long id);

    Long create(RoleCreateDTO dto);

    void update(Long id, RoleCreateDTO dto);

    void delete(Long id);

    List<String> getPermissions(Long roleId);

    void updatePermissions(Long roleId, List<String> codes);

    void assignUserRole(Long userId, Long roleId, Long orgId);
}
