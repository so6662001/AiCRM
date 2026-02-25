package com.aicrm.module.crm.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传视图VO
 */
@Data
@Schema(description = "文件上传结果")
public class FileUploadVO {

    @Schema(description = "文件ID")
    private Long fileId;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件URL")
    private String fileUrl;
    @Schema(description = "文件大小")
    private Long fileSize;
}
