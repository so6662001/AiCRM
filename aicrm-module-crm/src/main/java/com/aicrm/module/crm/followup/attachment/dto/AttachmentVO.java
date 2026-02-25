package com.aicrm.module.crm.followup.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进附件视图VO
 */
@Data
@Schema(description = "跟进附件视图")
public class AttachmentVO {

    private Long id;
    private Long tenantId;
    private Long followUpId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private Integer sortOrder;
    private LocalDateTime createdTime;
}
