package com.aicrm.module.crm.opportunity.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.opportunity.dto.StageConfigDTO;
import com.aicrm.module.crm.opportunity.dto.StageConfigVO;
import com.aicrm.module.crm.opportunity.entity.OpportunityStageConfig;
import com.aicrm.module.crm.opportunity.mapper.OpportunityStageConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商机阶段配置服务实现
 */
@Service
@RequiredArgsConstructor
public class OpportunityStageConfigServiceImpl implements OpportunityStageConfigService {

    private static final int DEFAULT_WIN_RATE = 0;
    private static final int DEFAULT_SORT_ORDER = 0;
    private static final int DEFAULT_IS_WON = 0;
    private static final int DEFAULT_IS_LOST = 0;
    private static final int DEFAULT_STATUS = 1;

    private final OpportunityStageConfigMapper stageConfigMapper;

    @Override
    public List<StageConfigVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<OpportunityStageConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityStageConfig::getTenantId, tenantId)
                .orderByAsc(OpportunityStageConfig::getSortOrder);

        List<OpportunityStageConfig> records = stageConfigMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StageConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        OpportunityStageConfig entity = new OpportunityStageConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setTenantId(tenantId);
        entity.setWinRate(dto.getWinRate() != null ? dto.getWinRate() : DEFAULT_WIN_RATE);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : DEFAULT_SORT_ORDER);
        entity.setIsWon(dto.getIsWon() != null ? dto.getIsWon() : DEFAULT_IS_WON);
        entity.setIsLost(dto.getIsLost() != null ? dto.getIsLost() : DEFAULT_IS_LOST);
        entity.setStatus(DEFAULT_STATUS);

        stageConfigMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StageConfigDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        OpportunityStageConfig exist = stageConfigMapper.selectOne(
                new LambdaQueryWrapper<OpportunityStageConfig>()
                        .eq(OpportunityStageConfig::getId, id)
                        .eq(OpportunityStageConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("商机阶段配置");
        }

        OpportunityStageConfig entity = new OpportunityStageConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        if (dto.getWinRate() != null) {
            entity.setWinRate(dto.getWinRate());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        if (dto.getIsWon() != null) {
            entity.setIsWon(dto.getIsWon());
        }
        if (dto.getIsLost() != null) {
            entity.setIsLost(dto.getIsLost());
        }

        stageConfigMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        OpportunityStageConfig exist = stageConfigMapper.selectOne(
                new LambdaQueryWrapper<OpportunityStageConfig>()
                        .eq(OpportunityStageConfig::getId, id)
                        .eq(OpportunityStageConfig::getTenantId, tenantId)
        );
        if (exist == null) {
            throw BizException.notFound("商机阶段配置");
        }

        stageConfigMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            OpportunityStageConfig exist = stageConfigMapper.selectOne(
                    new LambdaQueryWrapper<OpportunityStageConfig>()
                            .eq(OpportunityStageConfig::getId, id)
                            .eq(OpportunityStageConfig::getTenantId, tenantId)
            );
            if (exist != null) {
                OpportunityStageConfig entity = new OpportunityStageConfig();
                entity.setId(id);
                entity.setSortOrder(i);
                stageConfigMapper.updateById(entity);
            }
        }
    }

    private StageConfigVO convertToVO(OpportunityStageConfig entity) {
        StageConfigVO vo = new StageConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
