package com.aicrm.module.fieldwork.recording.controller;

import com.aicrm.common.core.Result;
import com.aicrm.common.page.PageResult;
import com.aicrm.module.fieldwork.recording.dto.RecordingCreateDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingQueryDTO;
import com.aicrm.module.fieldwork.recording.dto.RecordingVO;
import com.aicrm.module.fieldwork.recording.service.RecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 电话录音Controller
 */
@RestController
@RequestMapping("/v1/call-recordings")
@RequiredArgsConstructor
@Tag(name = "电话录音")
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping
    @Operation(summary = "分页查询录音")
    public Result<PageResult<RecordingVO>> page(@Valid RecordingQueryDTO query) {
        return Result.ok(recordingService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取录音详情")
    public Result<RecordingVO> getById(@PathVariable Long id) {
        return Result.ok(recordingService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建录音")
    public Result<Long> create(@Valid @RequestBody RecordingCreateDTO dto) {
        return Result.ok(recordingService.create(dto));
    }

    @PostMapping("/{id}/transcribe")
    @Operation(summary = "触发转写")
    public Result<Void> triggerTranscribe(@PathVariable Long id) {
        recordingService.triggerTranscribe(id);
        return Result.ok();
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "触发分析")
    public Result<Void> triggerAnalyze(@PathVariable Long id) {
        recordingService.triggerAnalyze(id);
        return Result.ok();
    }

    @GetMapping("/violations")
    @Operation(summary = "违规录音列表")
    public Result<java.util.List<RecordingVO>> violations() {
        var query = new RecordingQueryDTO();
        query.setHasViolation(true);
        query.setPageSize(50);
        return Result.ok(recordingService.page(query).getRecords());
    }
}
