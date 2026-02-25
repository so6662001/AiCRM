package com.aicrm.module.crm.lead.assignlog.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.lead.assignlog.dto.AssignLogVO;
import com.aicrm.module.crm.lead.assignlog.entity.LeadAssignLog;
import com.aicrm.module.crm.lead.assignlog.mapper.LeadAssignLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线索分配日志服务实现
 */
@Service
@RequiredArgsConstructor
public class LeadAssignLogServiceImpl implements LeadAssignLogService {

    private final LeadAssignLogMapper leadAssignLogMapper;

    @Override
    public List<AssignLogVO> listByLeadId(Long leadId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<LeadAssignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeadAssignLog::getTenantId, tenantId)
                .eq(LeadAssignLog::getLeadId, leadId)
                .orderByDesc(LeadAssignLog::getCreatedTime);

        List<LeadAssignLog> records = leadAssignLogMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public void log(Long leadId, Integer assignType, Long fromUserId, Long toUserId, String remark) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        LeadAssignLog log = new LeadAssignLog();
        log.setTenantId(tenantId);
        log.setLeadId(leadId);
        log.setAssignType(assignType);
        log.setFromUserId(fromUserId);
        log.setToUserId(toUserId);
        log.setAssignBy(TenantContext.getUserId());
        log.setRemark(remark);
        log.setCreatedTime(LocalDateTime.now());

        leadAssignLogMapper.insert(log);
    }

    private AssignLogVO convertToVO(LeadAssignLog entity) {
        AssignLogVO vo = new AssignLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setFromUserName(null); // TODO: 从用户服务获取
        vo.setToUserName(null);   // TODO: 从用户服务获取
        vo.setAssignByName(null); // TODO: 从用户服务获取
        return vo;
    }
}
