package com.aicrm.module.crm.customer.service;

import com.aicrm.module.crm.customer.dto.ApprovalVO;

import java.util.List;

/**
 * 客户审核服务接口
 */
public interface CustomerApprovalService {

    /**
     * 查询审核列表
     */
    List<ApprovalVO> list();

    /**
     * 提交审核
     */
    Long submitApproval(Long customerId, Integer type);

    /**
     * 通过
     */
    void approve(Long id, String remark);

    /**
     * 拒绝
     */
    void reject(Long id, String remark);
}
