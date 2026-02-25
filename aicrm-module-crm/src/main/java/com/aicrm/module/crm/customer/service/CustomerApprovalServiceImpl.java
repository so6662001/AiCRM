package com.aicrm.module.crm.customer.service;

import cn.hutool.core.bean.BeanUtil;
import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.customer.dto.ApprovalVO;
import com.aicrm.module.crm.customer.entity.Customer;
import com.aicrm.module.crm.customer.entity.CustomerApproval;
import com.aicrm.module.crm.customer.mapper.CustomerApprovalMapper;
import com.aicrm.module.crm.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 客户审核服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomerApprovalServiceImpl implements CustomerApprovalService {

    private static final int STATUS_PENDING = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_REJECTED = 3;

    private final CustomerApprovalMapper customerApprovalMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<ApprovalVO> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }

        LambdaQueryWrapper<CustomerApproval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerApproval::getTenantId, tenantId)
                .orderByDesc(CustomerApproval::getCreatedTime);

        List<CustomerApproval> approvals = customerApprovalMapper.selectList(wrapper);
        return approvals.stream().map(this::convertToVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApproval(Long customerId, Integer type) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            throw new BizException("租户或用户ID不能为空");
        }

        ensureCustomerExists(tenantId, customerId);

        CustomerApproval approval = new CustomerApproval();
        approval.setTenantId(tenantId);
        approval.setCustomerId(customerId);
        approval.setApprovalType(type);
        approval.setStatus(STATUS_PENDING);
        approval.setApplicantId(userId);
        approval.setCreatedTime(LocalDateTime.now());

        customerApprovalMapper.insert(approval);
        return approval.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String remark) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            throw new BizException("租户或用户ID不能为空");
        }

        CustomerApproval existing = customerApprovalMapper.selectOne(
                new LambdaQueryWrapper<CustomerApproval>()
                        .eq(CustomerApproval::getId, id)
                        .eq(CustomerApproval::getTenantId, tenantId)
        );
        if (existing == null) {
            throw BizException.notFound("审核记录");
        }
        if (!Objects.equals(existing.getStatus(), STATUS_PENDING)) {
            throw new BizException("该审核已处理");
        }

        LambdaUpdateWrapper<CustomerApproval> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CustomerApproval::getId, id)
                .eq(CustomerApproval::getTenantId, tenantId)
                .set(CustomerApproval::getStatus, STATUS_APPROVED)
                .set(CustomerApproval::getApproverId, userId)
                .set(CustomerApproval::getApproveTime, LocalDateTime.now())
                .set(CustomerApproval::getApproveRemark, remark);

        customerApprovalMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        Long tenantId = TenantContext.getTenantId();
        Long userId = TenantContext.getUserId();
        if (tenantId == null || userId == null) {
            throw new BizException("租户或用户ID不能为空");
        }

        CustomerApproval existing = customerApprovalMapper.selectOne(
                new LambdaQueryWrapper<CustomerApproval>()
                        .eq(CustomerApproval::getId, id)
                        .eq(CustomerApproval::getTenantId, tenantId)
        );
        if (existing == null) {
            throw BizException.notFound("审核记录");
        }
        if (!Objects.equals(existing.getStatus(), STATUS_PENDING)) {
            throw new BizException("该审核已处理");
        }

        LambdaUpdateWrapper<CustomerApproval> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CustomerApproval::getId, id)
                .eq(CustomerApproval::getTenantId, tenantId)
                .set(CustomerApproval::getStatus, STATUS_REJECTED)
                .set(CustomerApproval::getApproverId, userId)
                .set(CustomerApproval::getApproveTime, LocalDateTime.now())
                .set(CustomerApproval::getApproveRemark, remark);

        customerApprovalMapper.update(null, wrapper);
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

    private ApprovalVO convertToVO(CustomerApproval approval) {
        ApprovalVO vo = new ApprovalVO();
        BeanUtil.copyProperties(approval, vo);
        return vo;
    }
}
