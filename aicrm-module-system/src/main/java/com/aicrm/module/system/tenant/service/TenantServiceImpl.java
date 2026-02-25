package com.aicrm.module.system.tenant.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.module.system.tenant.dto.TenantCreateDTO;
import com.aicrm.module.system.tenant.dto.TenantVO;
import com.aicrm.module.system.tenant.entity.Tenant;
import com.aicrm.module.system.tenant.mapper.TenantMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租户服务实现
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    @Override
    public List<TenantVO> list() {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Tenant::getCreatedTime);
        List<Tenant> list = tenantMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).toList();
    }

    @Override
    public TenantVO getById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        return tenant != null ? convertToVO(tenant) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TenantCreateDTO dto) {
        Tenant tenant = new Tenant();
        BeanUtil.copyProperties(dto, tenant);
        tenant.setStatus(1);
        tenant.setStorageQuotaMb(0L);
        tenant.setStorageUsedMb(0L);
        tenantMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TenantCreateDTO dto) {
        Tenant existing = tenantMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("租户不存在");
        }
        Tenant tenant = new Tenant();
        BeanUtil.copyProperties(dto, tenant);
        tenant.setId(id);
        tenantMapper.updateById(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Tenant existing = tenantMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("租户不存在");
        }
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);
    }

    private TenantVO convertToVO(Tenant tenant) {
        TenantVO vo = new TenantVO();
        BeanUtil.copyProperties(tenant, vo);
        return vo;
    }
}
