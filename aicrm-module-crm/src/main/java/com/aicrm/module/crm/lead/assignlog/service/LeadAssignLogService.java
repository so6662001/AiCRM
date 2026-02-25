package com.aicrm.module.crm.lead.assignlog.service;

import com.aicrm.module.crm.lead.assignlog.dto.AssignLogVO;

import java.util.List;

/**
 * 线索分配日志服务
 */
public interface LeadAssignLogService {

    /**
     * 按线索ID查询分配日志列表
     */
    List<AssignLogVO> listByLeadId(Long leadId);

    /**
     * 记录分配日志
     */
    void log(Long leadId, Integer assignType, Long fromUserId, Long toUserId, String remark);
}
