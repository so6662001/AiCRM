package com.aicrm.module.crm.followup.attachment.service;

import com.aicrm.common.exception.BizException;
import com.aicrm.common.tenant.TenantContext;
import com.aicrm.module.crm.followup.attachment.dto.AttachmentCreateDTO;
import com.aicrm.module.crm.followup.attachment.dto.AttachmentVO;
import com.aicrm.module.crm.followup.attachment.entity.FollowUpAttachment;
import com.aicrm.module.crm.followup.attachment.mapper.FollowUpAttachmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跟进附件服务实现
 */
@Service
@RequiredArgsConstructor
public class FollowUpAttachmentServiceImpl implements FollowUpAttachmentService {

    private final FollowUpAttachmentMapper followUpAttachmentMapper;

    @Override
    public List<AttachmentVO> listByFollowUpId(Long followUpId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<FollowUpAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpAttachment::getTenantId, tenantId)
                .eq(FollowUpAttachment::getFollowUpId, followUpId)
                .orderByAsc(FollowUpAttachment::getSortOrder);

        List<FollowUpAttachment> records = followUpAttachmentMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public Long create(AttachmentCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        FollowUpAttachment entity = new FollowUpAttachment();
        entity.setTenantId(tenantId);
        entity.setFollowUpId(dto.getFollowUpId());
        entity.setFileName(dto.getFileName());
        entity.setFileType(dto.getFileType());
        entity.setFileSize(dto.getFileSize());
        entity.setFileUrl(dto.getFileUrl());
        entity.setSortOrder(0);
        entity.setCreatedTime(LocalDateTime.now());

        followUpAttachmentMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void delete(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户ID不能为空");
        }

        LambdaQueryWrapper<FollowUpAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpAttachment::getId, id)
                .eq(FollowUpAttachment::getTenantId, tenantId);

        followUpAttachmentMapper.delete(wrapper);
    }

    private AttachmentVO convertToVO(FollowUpAttachment entity) {
        AttachmentVO vo = new AttachmentVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
