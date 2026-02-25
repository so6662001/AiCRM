package com.aicrm.module.crm.opportunity.stagelog.service;

import com.aicrm.module.crm.opportunity.stagelog.dto.StageLogVO;

import java.util.List;

/**
 * 商机阶段变更日志服务
 */
public interface OpportunityStageLogService {

    /**
     * 按商机ID查询阶段变更日志列表
     */
    List<StageLogVO> listByOpportunityId(Long opportunityId);

    /**
     * 记录阶段变更日志
     */
    void log(Long opportunityId, Long fromStageId, Long toStageId, Integer stayDays, String remark);
}
