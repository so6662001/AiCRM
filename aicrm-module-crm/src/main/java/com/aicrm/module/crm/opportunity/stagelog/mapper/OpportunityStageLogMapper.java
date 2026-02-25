package com.aicrm.module.crm.opportunity.stagelog.mapper;

import com.aicrm.module.crm.opportunity.stagelog.entity.OpportunityStageLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商机阶段变更日志Mapper
 */
@Mapper
public interface OpportunityStageLogMapper extends BaseMapper<OpportunityStageLog> {
}
