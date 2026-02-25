package com.aicrm.module.crm.opportunity.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.crm.opportunity.dto.*;
import com.aicrm.module.crm.opportunity.entity.Opportunity;
import com.aicrm.module.crm.opportunity.entity.OpportunityStageConfig;
import com.aicrm.module.crm.opportunity.mapper.OpportunityMapper;
import com.aicrm.module.crm.opportunity.mapper.OpportunityStageConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 商机服务实现
 */
@Service
@RequiredArgsConstructor
public class OpportunityServiceImpl implements OpportunityService {

    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_WON = 2;
    private static final int STATUS_LOST = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OpportunityMapper opportunityMapper;
    private final CustomerMapper customerMapper;
    private final OpportunityStageConfigMapper stageConfigMapper;

    @Override
    public PageResult<OpportunityVO> page(OpportunityQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opportunity::getTenantId, tenantId)
                .eq(query.getCustomerId() != null, Opportunity::getCustomerId, query.getCustomerId())
                .eq(query.getStageId() != null, Opportunity::getStageId, query.getStageId())
                .eq(query.getStatus() != null, Opportunity::getStatus, query.getStatus())
                .eq(query.getOwnerUserId() != null, Opportunity::getOwnerUserId, query.getOwnerUserId())
                .ge(query.getExpectedCloseDateStart() != null, Opportunity::getExpectedCloseDate, query.getExpectedCloseDateStart())
                .le(query.getExpectedCloseDateEnd() != null, Opportunity::getExpectedCloseDate, query.getExpectedCloseDateEnd());

        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(Opportunity::getOpportunityNo, query.getKeyword())
                    .or().like(Opportunity::getOpportunityName, query.getKeyword()));
        }

        wrapper.orderByDesc(Opportunity::getCreatedTime);

        Page<Opportunity> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Opportunity> result = opportunityMapper.selectPage(page, wrapper);

        List<OpportunityVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public OpportunityVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return null;
        }

        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opportunity::getId, id).eq(Opportunity::getTenantId, tenantId);
        Opportunity opportunity = opportunityMapper.selectOne(wrapper);
        return opportunity != null ? convertToVO(opportunity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OpportunityCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();

        Opportunity opportunity = new Opportunity();
        BeanUtil.copyProperties(dto, opportunity);
        opportunity.setOpportunityNo(generateOpportunityNo(tenantId));
        opportunity.setStatus(STATUS_IN_PROGRESS);
        opportunity.setFollowCount(0);
        opportunity.setTenantId(tenantId);
        opportunity.setCreatedBy(userId);
        opportunity.setUpdatedBy(userId);
        opportunity.setOwnerUserId(userId);

        opportunityMapper.insert(opportunity);
        return opportunity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OpportunityUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Opportunity existing = opportunityMapper.selectOne(
                new LambdaQueryWrapper<Opportunity>()
                        .eq(Opportunity::getId, dto.getId())
                        .eq(Opportunity::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("商机不存在");
        }

        Opportunity opportunity = new Opportunity();
        BeanUtil.copyProperties(dto, opportunity, CopyOptions.create().ignoreNullValue());
        opportunity.setId(dto.getId());
        opportunity.setUpdatedBy(TenantContext.getUserId());

        opportunityMapper.updateById(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opportunity::getId, id).eq(Opportunity::getTenantId, tenantId);
        opportunityMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStage(Long id, StageChangeDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Opportunity existing = opportunityMapper.selectOne(
                new LambdaQueryWrapper<Opportunity>()
                        .eq(Opportunity::getId, id)
                        .eq(Opportunity::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("商机不存在");
        }

        LambdaUpdateWrapper<Opportunity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Opportunity::getId, id)
                .eq(Opportunity::getTenantId, tenantId)
                .set(Opportunity::getStageId, dto.getStageId())
                .set(Opportunity::getUpdatedBy, TenantContext.getUserId())
                .set(Opportunity::getUpdatedTime, LocalDateTime.now());
        if (StrUtil.isNotBlank(dto.getRemark())) {
            wrapper.set(Opportunity::getRemark, dto.getRemark());
        }
        opportunityMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void win(Long id, java.math.BigDecimal actualAmount) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Opportunity existing = opportunityMapper.selectOne(
                new LambdaQueryWrapper<Opportunity>()
                        .eq(Opportunity::getId, id)
                        .eq(Opportunity::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("商机不存在");
        }

        LambdaUpdateWrapper<Opportunity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Opportunity::getId, id)
                .eq(Opportunity::getTenantId, tenantId)
                .set(Opportunity::getStatus, STATUS_WON)
                .set(Opportunity::getActualAmount, actualAmount)
                .set(Opportunity::getActualCloseDate, LocalDate.now())
                .set(Opportunity::getUpdatedBy, TenantContext.getUserId())
                .set(Opportunity::getUpdatedTime, LocalDateTime.now());
        opportunityMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lose(Long id, String lossReason) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Opportunity existing = opportunityMapper.selectOne(
                new LambdaQueryWrapper<Opportunity>()
                        .eq(Opportunity::getId, id)
                        .eq(Opportunity::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("商机不存在");
        }

        LambdaUpdateWrapper<Opportunity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Opportunity::getId, id)
                .eq(Opportunity::getTenantId, tenantId)
                .set(Opportunity::getStatus, STATUS_LOST)
                .set(Opportunity::getLossReason, lossReason)
                .set(Opportunity::getUpdatedBy, TenantContext.getUserId())
                .set(Opportunity::getUpdatedTime, LocalDateTime.now());
        opportunityMapper.update(null, wrapper);
    }

    private OpportunityVO convertToVO(Opportunity opportunity) {
        OpportunityVO vo = new OpportunityVO();
        BeanUtil.copyProperties(opportunity, vo);
        vo.setCustomerName(resolveCustomerName(opportunity.getCustomerId()));
        vo.setStageName(resolveStageName(opportunity.getStageId()));
        vo.setOwnerUserName(resolveOwnerUserName(opportunity.getOwnerUserId()));
        return vo;
    }

    private String resolveCustomerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = customerMapper.selectById(customerId);
        return customer != null ? customer.getCustomerName() : null;
    }

    private String resolveStageName(Long stageId) {
        if (stageId == null) {
            return null;
        }
        OpportunityStageConfig config = stageConfigMapper.selectById(stageId);
        return config != null ? config.getStageName() : null;
    }

    private String resolveOwnerUserName(Long ownerUserId) {
        if (ownerUserId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名，暂时返回空
        return null;
    }

    private String generateOpportunityNo(Long tenantId) {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "OP" + dateStr;

        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opportunity::getTenantId, tenantId)
                .likeRight(Opportunity::getOpportunityNo, prefix)
                .orderByDesc(Opportunity::getOpportunityNo)
                .last("LIMIT 1");

        Opportunity last = opportunityMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getOpportunityNo() != null && last.getOpportunityNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getOpportunityNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
