package com.aicrm.module.crm.file.service;

import com.aicrm.module.crm.file.dto.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务
 */
public interface FileService {

    FileUploadVO upload(MultipartFile file);

    String getDownloadUrl(Long id);

    String getPresignedUrl(Long id);
}
