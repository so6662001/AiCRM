package com.aicrm.module.crm.customer.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.dto.*;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
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
import java.util.Objects;

/**
 * 客户服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final int LIFECYCLE_INVALID = 7;
    private static final int LIFECYCLE_POTENTIAL = 1;

    private final CustomerMapper customerMapper;

    @Override
    public PageResult<CustomerVO> page(CustomerQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return PageResult.of(List.of(), 0, query.getPageNum(), query.getPageSize());
        }

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getTenantId, tenantId);

        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(Customer::getCustomerName, query.getKeyword())
                    .or()
                    .like(Customer::getShortName, query.getKeyword()));
        }
        if (query.getLifecycleStage() != null) {
            wrapper.eq(Customer::getLifecycleStage, query.getLifecycleStage());
        }
        if (StrUtil.isNotBlank(query.getLevel())) {
            wrapper.eq(Customer::getLevel, query.getLevel());
        }
        if (query.getOwnerUserId() != null) {
            wrapper.eq(Customer::getOwnerUserId, query.getOwnerUserId());
        }
        if (StrUtil.isNotBlank(query.getIndustry())) {
            wrapper.eq(Customer::getIndustry, query.getIndustry());
        }
        if (StrUtil.isNotBlank(query.getPurchaseStatus())) {
            LocalDate today = LocalDate.now();
            if ("upcoming".equals(query.getPurchaseStatus())) {
                wrapper.isNotNull(Customer::getExpectedPurchaseDate)
                        .ge(Customer::getExpectedPurchaseDate, today);
                if (query.getPurchaseDaysWithin() != null && query.getPurchaseDaysWithin() > 0) {
                    wrapper.le(Customer::getExpectedPurchaseDate, today.plusDays(query.getPurchaseDaysWithin()));
                }
            } else if ("overdue".equals(query.getPurchaseStatus())) {
                wrapper.isNotNull(Customer::getExpectedPurchaseDate)
                        .lt(Customer::getExpectedPurchaseDate, today);
            }
        }
        if (query.getCreatedTimeStart() != null) {
            wrapper.ge(Customer::getCreatedTime, query.getCreatedTimeStart());
        }
        if (query.getCreatedTimeEnd() != null) {
            wrapper.le(Customer::getCreatedTime, query.getCreatedTimeEnd());
        }

        wrapper.orderByDesc(Customer::getCreatedTime);

        Page<Customer> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Customer> result = customerMapper.selectPage(page, wrapper);

        List<CustomerVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public CustomerVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return null;
        }

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getId, id).eq(Customer::getTenantId, tenantId);
        Customer customer = customerMapper.selectOne(wrapper);
        return customer != null ? convertToVO(customer) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerCreateDTO dto) {
        Customer customer = new Customer();
        BeanUtil.copyProperties(dto, customer);
        customer.setCustomerNo(generateCustomerNo());
        customer.setLifecycleStage(LIFECYCLE_POTENTIAL);
        customer.setFollowCount(0);
        customer.setDealCount(0);
        customer.setTenantId(TenantContext.getTenantId());
        customer.setCreatedBy(TenantContext.getUserId());
        customer.setUpdatedBy(TenantContext.getUserId());

        customerMapper.insert(customer);
        return customer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CustomerUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        Customer existing = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getId, dto.getId())
                        .eq(Customer::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("客户不存在");
        }

        Customer customer = new Customer();
        BeanUtil.copyProperties(dto, customer, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("id"));
        customer.setId(dto.getId());
        customer.setUpdatedBy(TenantContext.getUserId());

        customerMapper.updateById(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaUpdateWrapper<Customer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Customer::getId, id).eq(Customer::getTenantId, tenantId);
        customerMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(List<Long> customerIds, Long targetUserId) {
        if (customerIds == null || customerIds.isEmpty()) {
            return;
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaUpdateWrapper<Customer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Customer::getId, customerIds)
                .eq(Customer::getTenantId, tenantId)
                .set(Customer::getOwnerUserId, targetUserId)
                .set(Customer::getUpdatedBy, TenantContext.getUserId())
                .set(Customer::getUpdatedTime, LocalDateTime.now());
        customerMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markInvalid(Long id, String reason) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaUpdateWrapper<Customer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Customer::getId, id)
                .eq(Customer::getTenantId, tenantId)
                .set(Customer::getLifecycleStage, LIFECYCLE_INVALID)
                .set(Customer::getRemark, reason)
                .set(Customer::getUpdatedBy, TenantContext.getUserId())
                .set(Customer::getUpdatedTime, LocalDateTime.now());
        customerMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reactivate(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaUpdateWrapper<Customer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Customer::getId, id)
                .eq(Customer::getTenantId, tenantId)
                .set(Customer::getLifecycleStage, LIFECYCLE_POTENTIAL)
                .set(Customer::getUpdatedBy, TenantContext.getUserId())
                .set(Customer::getUpdatedTime, LocalDateTime.now());
        customerMapper.update(null, wrapper);
    }

    private CustomerVO convertToVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        BeanUtil.copyProperties(customer, vo);
        vo.setOwnerUserName(resolveOwnerUserName(customer.getOwnerUserId()));
        vo.setPurchaseCountdown(calculatePurchaseCountdown(customer.getExpectedPurchaseDate()));
        return vo;
    }

    private String resolveOwnerUserName(Long ownerUserId) {
        if (ownerUserId == null) {
            return null;
        }
        // TODO: 集成用户服务获取用户名，暂时返回空
        return null;
    }

    private PurchaseCountdownVO calculatePurchaseCountdown(LocalDate expectedPurchaseDate) {
        if (expectedPurchaseDate == null) {
            return PurchaseCountdownVO.builder()
                    .status("none")
                    .daysRemaining(null)
                    .daysOverdue(null)
                    .displayText("未设置")
                    .build();
        }

        LocalDate today = LocalDate.now();
        if (expectedPurchaseDate.isAfter(today)) {
            int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(today, expectedPurchaseDate);
            return PurchaseCountdownVO.builder()
                    .status("upcoming")
                    .daysRemaining(daysRemaining)
                    .daysOverdue(null)
                    .displayText(daysRemaining + "天后")
                    .build();
        } else if (expectedPurchaseDate.isBefore(today)) {
            int daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(expectedPurchaseDate, today);
            return PurchaseCountdownVO.builder()
                    .status("overdue")
                    .daysRemaining(null)
                    .daysOverdue(daysOverdue)
                    .displayText("逾期" + daysOverdue + "天")
                    .build();
        } else {
            return PurchaseCountdownVO.builder()
                    .status("upcoming")
                    .daysRemaining(0)
                    .daysOverdue(null)
                    .displayText("今天")
                    .build();
        }
    }

    private String generateCustomerNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CU" + datePrefix;

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getTenantId, TenantContext.getTenantId())
                .likeRight(Customer::getCustomerNo, prefix)
                .orderByDesc(Customer::getCustomerNo)
                .last("LIMIT 1");

        Customer last = customerMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getCustomerNo() != null && last.getCustomerNo().length() >= prefix.length() + 4) {
            try {
                seq = Integer.parseInt(last.getCustomerNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + String.format("%04d", Math.min(seq, 9999));
    }
}
