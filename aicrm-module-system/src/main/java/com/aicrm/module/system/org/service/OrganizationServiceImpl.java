package com.aicrm.module.system.org.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.system.org.dto.OrgCreateDTO;
import com.aicrm.module.system.org.dto.OrgVO;
import com.aicrm.module.system.org.entity.Organization;
import com.aicrm.module.system.org.mapper.OrganizationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 组织架构服务实现
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private static final Long ROOT_PARENT_ID = 0L;

    private final OrganizationMapper organizationMapper;

    @Override
    public List<OrgVO> tree() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getTenantId, tenantId)
                .orderByAsc(Organization::getSortOrder)
                .orderByAsc(Organization::getId);
        List<Organization> all = organizationMapper.selectList(wrapper);
        List<OrgVO> voList = all.stream().map(this::convertToVO).toList();
        return buildTree(voList, ROOT_PARENT_ID);
    }

    @Override
    public OrgVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return null;
        }

        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getId, id).eq(Organization::getTenantId, tenantId);
        Organization org = organizationMapper.selectOne(wrapper);
        return org != null ? convertToVO(org) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrgCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("租户上下文不存在");
        }

        Organization org = new Organization();
        BeanUtil.copyProperties(dto, org);
        org.setParentId(dto.getParentId() != null ? dto.getParentId() : ROOT_PARENT_ID);
        org.setTenantId(tenantId);
        org.setCreatedBy(TenantContext.getUserId());
        org.setUpdatedBy(TenantContext.getUserId());
        org.setStatus(1);

        organizationMapper.insert(org);

        String orgPath = calculateOrgPath(org.getId(), org.getParentId());
        org.setOrgPath(orgPath);
        organizationMapper.updateById(org);

        return org.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, OrgCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("租户上下文不存在");
        }

        Organization existing = organizationMapper.selectOne(
                new LambdaQueryWrapper<Organization>()
                        .eq(Organization::getId, id)
                        .eq(Organization::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("组织不存在");
        }

        Organization org = new Organization();
        BeanUtil.copyProperties(dto, org);
        org.setId(id);
        org.setUpdatedBy(TenantContext.getUserId());
        organizationMapper.updateById(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("租户上下文不存在");
        }

        Organization existing = organizationMapper.selectOne(
                new LambdaQueryWrapper<Organization>()
                        .eq(Organization::getId, id)
                        .eq(Organization::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("组织不存在");
        }

        organizationMapper.deleteById(id);
    }

    @Override
    public List<Object> listMembers(Long orgId) {
        return List.of();
    }

    private String calculateOrgPath(Long id, Long parentId) {
        if (parentId == null || ROOT_PARENT_ID.equals(parentId)) {
            return "/" + id + "/";
        }
        Organization parent = organizationMapper.selectById(parentId);
        if (parent == null || parent.getOrgPath() == null) {
            return "/" + id + "/";
        }
        return parent.getOrgPath() + id + "/";
    }

    private List<OrgVO> buildTree(List<OrgVO> list, Long parentId) {
        List<OrgVO> result = new ArrayList<>();
        for (OrgVO vo : list) {
            Long pid = vo.getParentId();
            if (Objects.equals(pid, parentId) || (pid == null && ROOT_PARENT_ID.equals(parentId))) {
                vo.setChildren(buildTree(list, vo.getId()));
                result.add(vo);
            }
        }
        return result;
    }

    private OrgVO convertToVO(Organization org) {
        OrgVO vo = new OrgVO();
        BeanUtil.copyProperties(org, vo);
        return vo;
    }
}
