package com.aicrm.module.crm.opportunity.service;

import com.aicrm.module.crm.opportunity.dto.StageConfigDTO;
import com.aicrm.module.crm.opportunity.dto.StageConfigVO;

import java.util.List;

/**
 * 商机阶段配置服务
 */
public interface OpportunityStageConfigService {

    List<StageConfigVO> list();

    Long create(StageConfigDTO dto);

    void update(Long id, StageConfigDTO dto);

    void delete(Long id);

    void sort(List<Long> ids);
}
