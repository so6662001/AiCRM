package com.aicrm.module.crm.erp.config.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.erp.config.dto.ErpConfigDTO;
import com.aicrm.module.crm.erp.config.dto.ErpConfigVO;
import com.aicrm.module.crm.erp.config.entity.TenantErpConfig;
import com.aicrm.module.crm.erp.config.mapper.TenantErpConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ERP配置服务实现
 */
@Service
@RequiredArgsConstructor
public class ErpConfigServiceImpl implements ErpConfigService {

    private static final String DEFAULT_AUTH_TYPE = "api_key";
    private static final String DEFAULT_SYNC_STRATEGY = "manual";
    private static final int DEFAULT_STATUS = 1;

    private final TenantErpConfigMapper erpConfigMapper;

    @Override
    public List<ErpConfigVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<TenantErpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantErpConfig::getTenantId, tenantId)
                .orderByDesc(TenantErpConfig::getCreatedTime);

        List<TenantErpConfig> records = erpConfigMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public ErpConfigVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        TenantErpConfig config = erpConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantErpConfig>()
                        .eq(TenantErpConfig::getId, id)
                        .eq(TenantErpConfig::getTenantId, tenantId)
        );
        if (config == null) {
            throw BizException.notFound("ERP配置");
        }
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ErpConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        TenantErpConfig entity = new TenantErpConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setAuthType(dto.getAuthType() != null ? dto.getAuthType() : DEFAULT_AUTH_TYPE);
        entity.setSyncStrategy(dto.getSyncStrategy() != null ? dto.getSyncStrategy() : DEFAULT_SYNC_STRATEGY);
        entity.setStatus(DEFAULT_STATUS);

        erpConfigMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ErpConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        TenantErpConfig exist = erpConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantErpConfig>()
                        .eq(TenantErpConfig::getId, id)
                        .eq(TenantErpConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("ERP配置");
        }

        TenantErpConfig entity = new TenantErpConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        if (dto.getAuthType() != null) {
            entity.setAuthType(dto.getAuthType());
        }
        if (dto.getSyncStrategy() != null) {
            entity.setSyncStrategy(dto.getSyncStrategy());
        }

        erpConfigMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        TenantErpConfig exist = erpConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantErpConfig>()
                        .eq(TenantErpConfig::getId, id)
                        .eq(TenantErpConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("ERP配置");
        }

        erpConfigMapper.deleteById(id);
    }

    @Override
    public boolean testConnection(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        TenantErpConfig exist = erpConfigMapper.selectOne(
                new LambdaQueryWrapper<TenantErpConfig>()
                        .eq(TenantErpConfig::getId, id)
                        .eq(TenantErpConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("ERP配置");
        }

        // DEMO版直接返回true
        return true;
    }

    private ErpConfigVO convertToVO(TenantErpConfig entity) {
        ErpConfigVO vo = new ErpConfigVO();
        BeanUtils.copyProperties(entity, vo);
        // authConfig脱敏
        if (entity.getAuthConfig() != null && !entity.getAuthConfig().isEmpty()) {
            vo.setAuthConfig("******");
        }
        return vo;
    }
}
