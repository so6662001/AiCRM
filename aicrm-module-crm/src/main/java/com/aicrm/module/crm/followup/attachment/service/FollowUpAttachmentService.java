package com.aicrm.module.crm.followup.attachment.service;

import com.aicrm.module.crm.followup.attachment.dto.AttachmentCreateDTO;
import com.aicrm.module.crm.followup.attachment.dto.AttachmentVO;

import java.util.List;

/**
 * 跟进附件服务
 */
public interface FollowUpAttachmentService {

    /**
     * 按跟进记录ID查询附件列表
     */
    List<AttachmentVO> listByFollowUpId(Long followUpId);

    /**
     * 创建附件
     */
    Long create(AttachmentCreateDTO dto);

    /**
     * 删除附件
     */
    void delete(Long id);
}
