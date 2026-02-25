package com.aicrm.module.crm.followup.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 跟进附件创建DTO
 */
@Data
@Schema(description = "跟进附件创建请求")
public class AttachmentCreateDTO {

    @NotNull(message = "跟进记录ID不能为空")
    @Schema(description = "跟进记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long followUpId;

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "文件类型不能为空")
    @Schema(description = "文件类型 image/audio/video/document", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileType;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @NotBlank(message = "文件URL不能为空")
    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;
}
