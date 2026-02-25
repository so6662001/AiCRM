package com.aicrm.module.crm.customer.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.dto.ContactCreateDTO;
import com.aicrm.module.crm.customer.dto.ContactVO;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.entity.CustomerContact;
import com.aicrm.module.crm.customer.mapper.CustomerContactMapper;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系人服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomerContactServiceImpl implements CustomerContactService {

    private final CustomerContactMapper customerContactMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<ContactVO> list(Long customerId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<CustomerContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerContact::getTenantId, tenantId)
                .eq(CustomerContact::getCustomerId, customerId)
                .orderByDesc(CustomerContact::getIsPrimary)
                .orderByAsc(CustomerContact::getCreatedTime);

        List<CustomerContact> contacts = customerContactMapper.selectList(wrapper);
        return contacts.stream().map(this::convertToVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long customerId, ContactCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        ensureCustomerExists(tenantId, customerId);

        CustomerContact contact = new CustomerContact();
        BeanUtil.copyProperties(dto, contact);
        contact.setCustomerId(customerId);
        contact.setTenantId(tenantId);
        contact.setCreatedBy(TenantContext.getUserId());
        contact.setUpdatedBy(TenantContext.getUserId());

        customerContactMapper.insert(contact);
        return contact.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long customerId, Long contactId, ContactCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        CustomerContact existing = customerContactMapper.selectOne(
                new LambdaQueryWrapper<CustomerContact>()
                        .eq(CustomerContact::getId, contactId)
                        .eq(CustomerContact::getTenantId, tenantId)
                        .eq(CustomerContact::getCustomerId, customerId)
        );
        if (existing == null) {
            throw BizException.notFound("联系人");
        }

        CustomerContact contact = new CustomerContact();
        BeanUtil.copyProperties(dto, contact);
        contact.setId(contactId);
        contact.setUpdatedBy(TenantContext.getUserId());

        customerContactMapper.updateById(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long customerId, Long contactId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LambdaQueryWrapper<CustomerContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerContact::getId, contactId)
                .eq(CustomerContact::getTenantId, tenantId)
                .eq(CustomerContact::getCustomerId, customerId);

        customerContactMapper.delete(wrapper);
    }

    private void ensureCustomerExists(Long tenantId, Long customerId) {
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getId, customerId)
                        .eq(Customer::getTenantId, tenantId)
        );
        if (customer == null) {
            throw BizException.notFound("客户");
        }
    }

    private ContactVO convertToVO(CustomerContact contact) {
        ContactVO vo = new ContactVO();
        BeanUtil.copyProperties(contact, vo);
        return vo;
    }
}
