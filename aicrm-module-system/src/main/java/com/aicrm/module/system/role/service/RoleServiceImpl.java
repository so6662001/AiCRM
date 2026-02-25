package com.aicrm.module.system.role.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.system.role.dto.RoleCreateDTO;
import com.aicrm.module.system.role.dto.RoleVO;
import com.aicrm.module.system.role.entity.Role;
import com.aicrm.module.system.role.entity.RolePermission;
import com.aicrm.module.system.role.entity.UserRole;
import com.aicrm.module.system.role.mapper.RoleMapper;
import com.aicrm.module.system.role.mapper.RolePermissionMapper;
import com.aicrm.module.system.role.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final int DEFAULT_ROLE_TYPE = 2;
    private static final int DEFAULT_DATA_SCOPE = 4;
    private static final int DEFAULT_STATUS = 1;

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<RoleVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getTenantId, tenantId)
                .orderByDesc(Role::getCreatedTime);

        List<Role> records = roleMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public RoleVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getId, id)
                        .eq(Role::getTenantId, tenantId)
        );
        if (role == null) {
            throw BizException.notFound("角色");
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setRoleType(DEFAULT_ROLE_TYPE);
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : DEFAULT_DATA_SCOPE);
        role.setStatus(DEFAULT_STATUS);

        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Role exist = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getId, id)
                        .eq(Role::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("角色");
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setId(id);
        if (dto.getDataScope() != null) {
            role.setDataScope(dto.getDataScope());
        }

        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Role exist = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getId, id)
                        .eq(Role::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("角色");
        }

        roleMapper.deleteById(id);
    }

    @Override
    public List<String> getPermissions(Long roleId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getTenantId, tenantId)
                .eq(RolePermission::getRoleId, roleId);

        List<RolePermission> list = rolePermissionMapper.selectList(wrapper);
        return list.stream()
                .map(RolePermission::getPermissionCode)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissions(Long roleId, List<String> codes) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getTenantId, tenantId)
                        .eq(RolePermission::getRoleId, roleId)
        );

        if (codes != null && !codes.isEmpty()) {
            for (String code : codes) {
                RolePermission rp = new RolePermission();
                rp.setTenantId(tenantId);
                rp.setRoleId(roleId);
                rp.setPermissionCode(code);
                rp.setCreatedTime(LocalDateTime.now());
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRole(Long userId, Long roleId, Long orgId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }
        if (orgId == null) {
            throw new BizException("组织ID不能为空");
        }

        UserRole userRole = new UserRole();
        userRole.setTenantId(tenantId);
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setOrgId(orgId);
        userRole.setCreatedTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
