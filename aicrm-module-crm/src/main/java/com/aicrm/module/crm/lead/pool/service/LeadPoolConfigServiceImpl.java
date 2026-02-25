package com.aicrm.module.crm.lead.pool.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.lead.pool.dto.PoolConfigDTO;
import com.aicrm.module.crm.lead.pool.dto.PoolConfigVO;
import com.aicrm.module.crm.lead.pool.entity.LeadPoolConfig;
import com.aicrm.module.crm.lead.pool.mapper.LeadPoolConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公海池配置服务实现
 */
@Service
@RequiredArgsConstructor
public class LeadPoolConfigServiceImpl implements LeadPoolConfigService {

    private static final int DEFAULT_RECYCLE_DAYS = 7;
    private static final int DEFAULT_MAX_HOLD_COUNT = 50;
    private static final int DEFAULT_DAILY_PICK_LIMIT = 5;
    private static final int DEFAULT_STATUS = 1;

    private final LeadPoolConfigMapper leadPoolConfigMapper;

    @Override
    public List<PoolConfigVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<LeadPoolConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeadPoolConfig::getTenantId, tenantId)
                .orderByDesc(LeadPoolConfig::getCreatedTime);

        List<LeadPoolConfig> records = leadPoolConfigMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PoolConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LeadPoolConfig entity = new LeadPoolConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setRecycleDays(dto.getRecycleDays() != null ? dto.getRecycleDays() : DEFAULT_RECYCLE_DAYS);
        entity.setMaxHoldCount(dto.getMaxHoldCount() != null ? dto.getMaxHoldCount() : DEFAULT_MAX_HOLD_COUNT);
        entity.setDailyPickLimit(dto.getDailyPickLimit() != null ? dto.getDailyPickLimit() : DEFAULT_DAILY_PICK_LIMIT);
        entity.setStatus(DEFAULT_STATUS);

        leadPoolConfigMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PoolConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LeadPoolConfig exist = leadPoolConfigMapper.selectOne(
                new LambdaQueryWrapper<LeadPoolConfig>()
                        .eq(LeadPoolConfig::getId, id)
                        .eq(LeadPoolConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("公海池配置");
        }

        LeadPoolConfig entity = new LeadPoolConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        if (dto.getRecycleDays() != null) {
            entity.setRecycleDays(dto.getRecycleDays());
        }
        if (dto.getMaxHoldCount() != null) {
            entity.setMaxHoldCount(dto.getMaxHoldCount());
        }
        if (dto.getDailyPickLimit() != null) {
            entity.setDailyPickLimit(dto.getDailyPickLimit());
        }

        leadPoolConfigMapper.updateById(entity);
    }

    private PoolConfigVO convertToVO(LeadPoolConfig entity) {
        PoolConfigVO vo = new PoolConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
