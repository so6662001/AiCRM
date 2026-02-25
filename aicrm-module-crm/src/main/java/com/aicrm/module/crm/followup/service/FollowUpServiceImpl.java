package com.aicrm.module.crm.followup.service;

import com.aicrm.common.page.PageResult;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.followup.dto.FollowUpCreateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpQueryDTO;
import com.aicrm.module.crm.followup.dto.FollowUpUpdateDTO;
import com.aicrm.module.crm.followup.dto.FollowUpVO;
import com.aicrm.module.crm.followup.entity.FollowUpRecord;
import com.aicrm.module.crm.followup.mapper.FollowUpRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 跟进记录服务实现
 */
@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpRecordMapper followUpRecordMapper;

    @Override
    public PageResult<FollowUpVO> page(FollowUpQueryDTO query) {
        LambdaQueryWrapper<FollowUpRecord> wrapper = buildQueryWrapper(query);
        Page<FollowUpRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<FollowUpRecord> result = followUpRecordMapper.selectPage(page, wrapper);
        List<FollowUpVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public FollowUpVO getById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        FollowUpRecord record = followUpRecordMapper.selectOne(
                new LambdaQueryWrapper<FollowUpRecord>()
                        .eq(FollowUpRecord::getId, id)
                        .eq(FollowUpRecord::getTenantId, tenantId)
        );
        if (record == null) {
            return null;
        }
        return toVO(record);
    }

    @Override
    public Long create(FollowUpCreateDTO dto) {
        FollowUpRecord record = new FollowUpRecord();
        BeanUtils.copyProperties(dto, record);
        record.setFollowUserId(TenantContext.getUserId());
        followUpRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void update(FollowUpUpdateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        FollowUpRecord existing = followUpRecordMapper.selectOne(
                new LambdaQueryWrapper<FollowUpRecord>()
                        .eq(FollowUpRecord::getId, dto.getId())
                        .eq(FollowUpRecord::getTenantId, tenantId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("跟进记录不存在");
        }
        if (dto.getContent() != null) {
            existing.setContent(dto.getContent());
        }
        if (dto.getNextFollowTime() != null) {
            existing.setNextFollowTime(dto.getNextFollowTime());
        }
        if (dto.getNextFollowNote() != null) {
            existing.setNextFollowNote(dto.getNextFollowNote());
        }
        followUpRecordMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        FollowUpRecord record = followUpRecordMapper.selectOne(
                new LambdaQueryWrapper<FollowUpRecord>()
                        .eq(FollowUpRecord::getId, id)
                        .eq(FollowUpRecord::getTenantId, tenantId)
        );
        if (record != null) {
            followUpRecordMapper.deleteById(id);
        }
    }

    private LambdaQueryWrapper<FollowUpRecord> buildQueryWrapper(FollowUpQueryDTO query) {
        Long tenantId = TenantContext.getTenantId();
        LambdaQueryWrapper<FollowUpRecord> wrapper = new LambdaQueryWrapper<FollowUpRecord>()
                .eq(FollowUpRecord::getTenantId, tenantId)
                .eq(query.getBizType() != null, FollowUpRecord::getBizType, query.getBizType())
                .eq(query.getBizId() != null, FollowUpRecord::getBizId, query.getBizId())
                .eq(query.getCustomerId() != null, FollowUpRecord::getCustomerId, query.getCustomerId())
                .eq(query.getFollowType() != null, FollowUpRecord::getFollowType, query.getFollowType())
                .eq(query.getFollowUserId() != null, FollowUpRecord::getFollowUserId, query.getFollowUserId())
                .eq(query.getHasViolation() != null, FollowUpRecord::getHasViolation, query.getHasViolation() ? 1 : 0)
                .ge(query.getCreatedTimeStart() != null, FollowUpRecord::getCreatedTime, query.getCreatedTimeStart())
                .le(query.getCreatedTimeEnd() != null, FollowUpRecord::getCreatedTime, query.getCreatedTimeEnd())
                .orderByDesc(FollowUpRecord::getCreatedTime);
        return wrapper;
    }

    private FollowUpVO toVO(FollowUpRecord record) {
        FollowUpVO vo = new FollowUpVO();
        BeanUtils.copyProperties(record, vo);
        // followUserName 和 bizName 需根据业务关联查询，此处暂留空
        return vo;
    }
}
