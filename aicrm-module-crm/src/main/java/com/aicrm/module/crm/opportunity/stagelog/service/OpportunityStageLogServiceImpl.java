package com.aicrm.module.crm.opportunity.stagelog.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.opportunity.stagelog.dto.StageLogVO;
import com.aicrm.module.crm.opportunity.stagelog.entity.OpportunityStageLog;
import com.aicrm.module.crm.opportunity.stagelog.mapper.OpportunityStageLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商机阶段变更日志服务实现
 */
@Service
@RequiredArgsConstructor
public class OpportunityStageLogServiceImpl implements OpportunityStageLogService {

    private final OpportunityStageLogMapper opportunityStageLogMapper;

    @Override
    public List<StageLogVO> listByOpportunityId(Long opportunityId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<OpportunityStageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityStageLog::getTenantId, tenantId)
                .eq(OpportunityStageLog::getOpportunityId, opportunityId)
                .orderByDesc(OpportunityStageLog::getCreatedTime);

        List<OpportunityStageLog> records = opportunityStageLogMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public void log(Long opportunityId, Long fromStageId, Long toStageId, Integer stayDays, String remark) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        OpportunityStageLog log = new OpportunityStageLog();
        log.setTenantId(tenantId);
        log.setOpportunityId(opportunityId);
        log.setFromStageId(fromStageId);
        log.setToStageId(toStageId);
        log.setStayDays(stayDays != null ? stayDays : 0);
        log.setRemark(remark);
        log.setOperatedBy(TenantContext.getUserId());
        log.setCreatedTime(java.time.LocalDateTime.now());

        opportunityStageLogMapper.insert(log);
    }

    private StageLogVO convertToVO(OpportunityStageLog entity) {
        StageLogVO vo = new StageLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setFromStageName(null);  // TODO: 从阶段配置获取
        vo.setToStageName(null);    // TODO: 从阶段配置获取
        vo.setOperatedByName(null); // TODO: 从用户服务获取
        return vo;
    }
}
