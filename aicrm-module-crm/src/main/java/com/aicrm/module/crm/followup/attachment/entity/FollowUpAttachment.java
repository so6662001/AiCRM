package com.aicrm.module.crm.followup.attachment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跟进附件实体（轻量表，不继承BaseEntity）
 */
@Data
@TableName("follow_up_attachment")
public class FollowUpAttachment implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 跟进记录ID */
    private Long followUpId;
    /** 文件名 */
    private String fileName;
    /** 文件类型 image/audio/video/document */
    private String fileType;
    /** 文件大小(字节) */
    private Long fileSize;
    /** 文件URL */
    private String fileUrl;
    /** 排序 默认0 */
    private Integer sortOrder;
    /** 创建时间 */
    private LocalDateTime createdTime;
}
