package com.aicrm.module.crm.file.controller;

import com.aicrm.common.core.Result;
import com.aicrm.module.crm.file.dto.FileUploadVO;
import com.aicrm.module.crm.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件服务控制器 - DEMO版模拟
 */
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Tag(name = "文件服务")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<FileUploadVO> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        return Result.ok(fileService.upload(file));
    }

    @PostMapping("/upload/image")
    @Operation(summary = "上传图片")
    public Result<FileUploadVO> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        return Result.ok(fileService.upload(file));
    }

    @PostMapping("/upload/recording")
    @Operation(summary = "上传录音")
    public Result<FileUploadVO> uploadRecording(@RequestParam(value = "file", required = false) MultipartFile file) {
        return Result.ok(fileService.upload(file));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "获取下载URL")
    public Result<Map<String, String>> download(@PathVariable Long id) {
        String url = fileService.getDownloadUrl(id);
        return Result.ok(Map.of("url", url));
    }

    @GetMapping("/{id}/presigned-url")
    @Operation(summary = "获取预签名URL")
    public Result<Map<String, String>> presignedUrl(@PathVariable Long id) {
        String url = fileService.getPresignedUrl(id);
        return Result.ok(Map.of("url", url));
    }
}
