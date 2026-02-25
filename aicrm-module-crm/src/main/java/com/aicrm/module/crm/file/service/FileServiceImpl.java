package com.aicrm.module.crm.file.service;

import com.aicrm.module.crm.file.dto.FileUploadVO;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务实现 - DEMO版模拟
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String DEMO_BASE_URL = "https://oss.example.com/demo/";

    @Override
    public FileUploadVO upload(MultipartFile file) {
        long fileId = IdUtil.getSnowflakeNextId();
        String fileName = file != null && file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "demo-file-" + fileId;
        long fileSize = file != null ? file.getSize() : 0;

        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(fileId);
        vo.setFileName(fileName);
        vo.setFileUrl(DEMO_BASE_URL + fileName);
        vo.setFileSize(fileSize);
        return vo;
    }

    @Override
    public String getDownloadUrl(Long id) {
        return DEMO_BASE_URL + "download/" + id + "?token=demo-token";
    }

    @Override
    public String getPresignedUrl(Long id) {
        return DEMO_BASE_URL + "presigned/" + id + "?expires=3600&token=demo-token";
    }
}
