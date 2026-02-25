package com.aicrm.module.crm.followup.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.module.crm.followup.dto.FollowUpCreateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpQueryDTO;
import com.aicrm.module.crm.followup.dto.FollowUpUpdateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpVO;

/**
 * 跟进记录服务接口
 */
public interface FollowUpService {

    /**
     * 分页查询跟进记录
     */
    PageResult<FollowUpVO> page(FollowUpQueryDTO query);

    /**
     * 根据ID获取跟进记录详情
     */
    FollowUpVO getById(Long id);

    /**
     * 创建跟进记录
     */
    Long create(FollowUpCreateDTO dto);

    /**
     * 更新跟进记录
     */
    void update(FollowUpUpdateDTO dto);

    /**
     * 删除跟进记录（逻辑删除）
     */
    void delete(Long id);
}
