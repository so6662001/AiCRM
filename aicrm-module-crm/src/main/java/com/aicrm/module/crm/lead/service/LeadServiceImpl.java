package com.aicrm.module.crm.lead.service;

import cn.hutool.core.util.StrUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.aicrm.module.crm.lead.dto.*;
import com.aicrm.module.crm.lead.entity.Lead;
import com.aicrm.module.crm.lead.mapper.LeadMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 线索服务实现类
 */
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private static final String LEAD_NO_PREFIX = "LD";
    private static final String CUSTOMER_NO_PREFIX = "CU";
    private static final int STATUS_CONVERTED = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LeadMapper leadMapper;
    private final CustomerMapper customerMapper;

    @Override
    public PageResult<LeadVO> page(LeadQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<Lead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Lead::getTenantId, tenantId)
                .eq(query.getStatus() != null, Lead::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getIntentionLevel()), Lead::getIntentionLevel, query.getIntentionLevel())
                .eq(StrUtil.isNotBlank(query.getSource()), Lead::getSource, query.getSource())
                .eq(query.getOwnerUserId() != null, Lead::getOwnerUserId, query.getOwnerUserId())
                .eq(query.getInPool() != null, Lead::getInPool, Boolean.TRUE.equals(query.getInPool()) ? 1 : 0)
                .ge(query.getCreatedTimeStart() != null, Lead::getCreatedTime, query.getCreatedTimeStart())
                .le(query.getCreatedTimeEnd() != null, Lead::getCreatedTime, query.getCreatedTimeEnd());

        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(Lead::getContactName, query.getKeyword())
                    .or().like(Lead::getContactPhone, query.getKeyword())
                    .or().like(Lead::getCompanyName, query.getKeyword()));
        }

        if (StrUtil.isNotBlank(query.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(query.getSortOrder());
            switch (query.getSortField()) {
                case "leadNo" -> wrapper.orderBy(true, isAsc, Lead::getLeadNo);
                case "contactName" -> wrapper.orderBy(true, isAsc, Lead::getContactName);
                case "companyName" -> wrapper.orderBy(true, isAsc, Lead::getCompanyName);
                case "createdTime" -> wrapper.orderBy(true, isAsc, Lead::getCreatedTime);
                case "assignTime" -> wrapper.orderBy(true, isAsc, Lead::getAssignTime);
                default -> wrapper.orderByDesc(Lead::getCreatedTime);
            }
        } else {
            wrapper.orderByDesc(Lead::getCreatedTime);
        }

        Page<Lead> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Lead> result = leadMapper.selectPage(page, wrapper);

        List<LeadVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public LeadVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead lead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getId, id)
                .eq(Lead::getTenantId, tenantId));

        if (lead == null) {
            throw BizException.notFound("线索");
        }

        return convertToVO(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(LeadCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead lead = new Lead();
        BeanUtils.copyProperties(dto, lead);
        lead.setLeadNo(generateLeadNo(tenantId));
        lead.setStatus(0);
        lead.setInPool(0);
        lead.setFollowCount(0);

        leadMapper.insert(lead);
        return lead.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LeadUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead existLead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getId, dto.getId())
                .eq(Lead::getTenantId, tenantId));

        if (existLead == null) {
            throw BizException.notFound("线索");
        }

        Lead lead = new Lead();
        BeanUtils.copyProperties(dto, lead);
        lead.setId(dto.getId());
        leadMapper.updateById(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead lead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getId, id)
                .eq(Lead::getTenantId, tenantId));

        if (lead == null) {
            throw BizException.notFound("线索");
        }

        leadMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(List<Long> leadIds, Long targetUserId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long leadId : leadIds) {
            Lead lead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                    .eq(Lead::getId, leadId)
                    .eq(Lead::getTenantId, tenantId));

            if (lead != null) {
                lead.setOwnerUserId(targetUserId);
                lead.setStatus(1);
                lead.setAssignUserId(TenantContext.getUserId());
                lead.setAssignTime(now);
                lead.setInPool(0);
                leadMapper.updateById(lead);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnToPool(Long id, String reason) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead lead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getId, id)
                .eq(Lead::getTenantId, tenantId));

        if (lead == null) {
            throw BizException.notFound("线索");
        }

        lead.setInPool(1);
        lead.setStatus(4);
        lead.setPoolEnterTime(LocalDateTime.now());
        lead.setReturnReason(reason);
        lead.setOwnerUserId(null);
        lead.setOwnerOrgId(null);
        leadMapper.updateById(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convert(Long id, String customerName, boolean createOpportunity) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        Lead lead = leadMapper.selectOne(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getId, id)
                .eq(Lead::getTenantId, tenantId));

        if (lead == null) {
            throw BizException.notFound("线索");
        }

        LocalDateTime now = LocalDateTime.now();
        lead.setStatus(STATUS_CONVERTED);
        lead.setConvertTime(now);
        leadMapper.updateById(lead);

        Customer customer = new Customer();
        customer.setCustomerName(customerName != null ? customerName : lead.getCompanyName());
        if (customer.getCustomerName() == null || customer.getCustomerName().isBlank()) {
            customer.setCustomerName("未命名客户");
        }
        customer.setOwnerUserId(lead.getOwnerUserId() != null ? lead.getOwnerUserId() : TenantContext.getUserId());
        customer.setOwnerOrgId(lead.getOwnerOrgId());
        customer.setSource("lead_convert");
        customer.setSourceLeadId(lead.getId());
        customer.setCustomerNo(generateCustomerNo(tenantId));
        customer.setLifecycleStage(1);
        customer.setFollowCount(0);
        customer.setDealCount(0);
        customer.setCustomerType(1);

        customerMapper.insert(customer);

        lead.setConvertCustomerId(customer.getId());
        leadMapper.updateById(lead);

        return customer.getId();
    }

    private String generateCustomerNo(Long tenantId) {
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        String prefix = CUSTOMER_NO_PREFIX + datePrefix;

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getTenantId, tenantId)
                .likeRight(Customer::getCustomerNo, prefix)
                .orderByDesc(Customer::getCustomerNo);

        Page<Customer> p = customerMapper.selectPage(new Page<>(1, 1), wrapper);
        Customer last = p.getRecords().isEmpty() ? null : p.getRecords().get(0);
        int seq = 1;
        if (last != null && last.getCustomerNo() != null && last.getCustomerNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getCustomerNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }

    private LeadVO convertToVO(Lead lead) {
        LeadVO vo = new LeadVO();
        BeanUtils.copyProperties(lead, vo);
        vo.setOwnerUserName(null); // TODO: 从用户服务获取负责人姓名
        return vo;
    }

    private String generateLeadNo(Long tenantId) {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = LEAD_NO_PREFIX + dateStr;

        LambdaQueryWrapper<Lead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Lead::getTenantId, tenantId)
                .likeRight(Lead::getLeadNo, prefix)
                .orderByDesc(Lead::getLeadNo);

        Page<Lead> p = leadMapper.selectPage(new Page<>(1, 1), wrapper);
        Lead lastLead = p.getRecords().isEmpty() ? null : p.getRecords().get(0);
        int seq = 1;
        if (lastLead != null && lastLead.getLeadNo() != null && lastLead.getLeadNo().length() >= prefix.length() + 4) {
            try {
                String seqStr = lastLead.getLeadNo().substring(prefix.length());
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException ignored) {
                // use default seq
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
